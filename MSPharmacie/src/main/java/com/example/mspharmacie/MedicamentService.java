package com.example.mspharmacie;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mspharmacie.client.PatientClient;
import com.example.mspharmacie.dto.CreateMedicamentRequest;
import com.example.mspharmacie.dto.DispenseRequestDto;
import com.example.mspharmacie.dto.MedicamentResponseDto;
import com.example.mspharmacie.dto.PatchStockRequest;
import com.example.mspharmacie.dto.StockInfoDto;
import com.example.mspharmacie.dto.UpdateMedicamentRequest;
import com.example.mspharmacie.messaging.StockAlertPublisher;
import com.example.mspharmacie.web.InsufficientStockException;
import com.example.mspharmacie.web.MedicamentNotFoundException;
import com.example.mspharmacie.web.PatientNotFoundException;

import feign.FeignException;

@Service
public class MedicamentService implements IMedicamentService {

	private static final Logger log = LoggerFactory.getLogger(MedicamentService.class);

	private final MedicamentRepository medicamentRepository;
	private final PatientClient patientClient;
	private final StockAlertPublisher stockAlertPublisher;

	@Value("${pharmacy.stock.low-threshold:5}")
	private int stockLowThreshold;

	public MedicamentService(MedicamentRepository medicamentRepository, PatientClient patientClient,
			StockAlertPublisher stockAlertPublisher) {
		this.medicamentRepository = medicamentRepository;
		this.patientClient = patientClient;
		this.stockAlertPublisher = stockAlertPublisher;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MedicamentResponseDto> findAllCatalog() {
		return medicamentRepository.findAll().stream().map(this::toCatalogDto).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public MedicamentResponseDto findCatalogById(int id) {
		Medicament m = medicamentRepository.findById(id).orElseThrow(() -> new MedicamentNotFoundException(id));
		return toCatalogDto(m);
	}

	@Override
	@Transactional(readOnly = true)
	public StockInfoDto getStockInfo(int id) {
		Medicament m = medicamentRepository.findById(id).orElseThrow(() -> new MedicamentNotFoundException(id));
		return new StockInfoDto(m.getId(), m.getStockQuantity(), m.getPrixUnitaire());
	}

	@Override
	@Transactional
	public MedicamentResponseDto create(CreateMedicamentRequest request) {
		Medicament entity = new Medicament(request.getNomMedicament(), request.getEtat(), request.getStockQuantity(),
				request.getPrixUnitaire());
		Medicament saved = medicamentRepository.save(entity);
		return toCatalogDto(saved);
	}

	@Override
	@Transactional
	public MedicamentResponseDto update(int id, UpdateMedicamentRequest request) {
		Medicament m = medicamentRepository.findById(id).orElseThrow(() -> new MedicamentNotFoundException(id));
		m.setNomMedicament(request.getNomMedicament());
		m.setEtat(request.getEtat());
		m.setPrixUnitaire(request.getPrixUnitaire());
		return toCatalogDto(medicamentRepository.save(m));
	}

	@Override
	@Transactional
	public MedicamentResponseDto patchStock(int id, PatchStockRequest request) {
		Medicament m = medicamentRepository.findById(id).orElseThrow(() -> new MedicamentNotFoundException(id));
		m.setStockQuantity(request.getQuantity());
		Medicament saved = medicamentRepository.save(m);
		maybeEmitStockLow(saved);
		return toCatalogDto(saved);
	}

	@Override
	@Transactional
	public MedicamentResponseDto dispense(int medicamentId, DispenseRequestDto request) {
		try {
			patientClient.getPatientById(request.getPatientId());
		}
		catch (FeignException.NotFound e) {
			throw new PatientNotFoundException(request.getPatientId());
		}
		catch (FeignException e) {
			if (e.status() == 404) {
				throw new PatientNotFoundException(request.getPatientId());
			}
			throw e;
		}

		Medicament m = medicamentRepository.findById(medicamentId)
			.orElseThrow(() -> new MedicamentNotFoundException(medicamentId));
		int qty = request.getQuantity();
		if (m.getStockQuantity() < qty) {
			throw new InsufficientStockException(
					"Stock insuffisant pour médicament " + medicamentId + " (demandé: " + qty + ")");
		}
		m.setStockQuantity(m.getStockQuantity() - qty);
		Medicament saved = medicamentRepository.save(m);
		maybeEmitStockLow(saved);
		return toCatalogDto(saved);
	}

	@Override
	@Transactional
	public void delete(int id) {
		if (!medicamentRepository.existsById(id)) {
			throw new MedicamentNotFoundException(id);
		}
		medicamentRepository.deleteById(id);
	}

	@Override
	@Transactional
	public void applyOrdonnanceStockConsumption(int medicamentId, int quantity) {
		Medicament m = medicamentRepository.findById(medicamentId).orElse(null);
		if (m == null) {
			log.warn("Événement ordonnance: médicament inconnu id={}", medicamentId);
			return;
		}
		int q = quantity <= 0 ? 1 : quantity;
		if (m.getStockQuantity() < q) {
			log.warn("Événement ordonnance: stock insuffisant médicament={} demandé={} disponible={}", medicamentId, q,
					m.getStockQuantity());
			return;
		}
		m.setStockQuantity(m.getStockQuantity() - q);
		Medicament saved = medicamentRepository.save(m);
		maybeEmitStockLow(saved);
	}

	private void maybeEmitStockLow(Medicament m) {
		if (m.getStockQuantity() >= stockLowThreshold) {
			return;
		}
		try {
			stockAlertPublisher.publish(m.getId(), m.getNomMedicament(), m.getStockQuantity());
		}
		catch (Exception ex) {
			log.warn("Impossible de publier l'alerte stock bas pour médicament {} : {}", m.getId(), ex.toString());
		}
	}

	private MedicamentResponseDto toCatalogDto(Medicament m) {
		return new MedicamentResponseDto(m.getId(), m.getNomMedicament(), m.isEtat());
	}
}
