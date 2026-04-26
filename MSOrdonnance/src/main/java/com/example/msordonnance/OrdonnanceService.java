package com.example.msordonnance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdonnanceService implements IOrdonnanceService {
    @Autowired
    private OrdonnanceRepository ordonnanceRepository;
    @Autowired
    private MedicamentClient medicamentClient;

    @Override
    public List<Ordonnance> getAll() {
        return ordonnanceRepository.findAll();
    }

    @Override
    public List<MedicamentDTO> getAllMedicaments() {
        return medicamentClient.getAll();
    }

    @Override
    public MedicamentDTO getMedicamentById(int id) {
        return medicamentClient.getMedicamentById(id);
    }

    @Override
    public List<MedicamentDTO> getMedicamentsByOrdonnance(int ordonnanceId) {
        Ordonnance ordonnance = ordonnanceRepository.findById(ordonnanceId).get();
        return ordonnance.getMedicamentsIds().stream()
                .map(medicamentClient::getMedicamentById)
                .collect(Collectors.toList());
    }

    @Override
    public void saveMedicamentToOrdonnance(int ordonnanceId, int medicamentId) {
        Ordonnance ordonnance = ordonnanceRepository.findById(ordonnanceId).get();
        ordonnance.getMedicamentsIds().add(medicamentId);
        ordonnanceRepository.save(ordonnance);
    }
}
