package com.example.mspharmacie;

import java.util.List;

import com.example.mspharmacie.dto.CreateMedicamentRequest;
import com.example.mspharmacie.dto.DispenseRequestDto;
import com.example.mspharmacie.dto.MedicamentResponseDto;
import com.example.mspharmacie.dto.PatchStockRequest;
import com.example.mspharmacie.dto.StockInfoDto;
import com.example.mspharmacie.dto.UpdateMedicamentRequest;

public interface IMedicamentService {

	List<MedicamentResponseDto> findAllCatalog();

	MedicamentResponseDto findCatalogById(int id);

	StockInfoDto getStockInfo(int id);

	MedicamentResponseDto create(CreateMedicamentRequest request);

	MedicamentResponseDto update(int id, UpdateMedicamentRequest request);

	MedicamentResponseDto patchStock(int id, PatchStockRequest request);

	MedicamentResponseDto dispense(int medicamentId, DispenseRequestDto request);

	void applyOrdonnanceStockConsumption(int medicamentId, int quantity);
}
