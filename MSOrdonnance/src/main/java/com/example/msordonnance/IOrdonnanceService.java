package com.example.msordonnance;

import java.util.List;

public interface IOrdonnanceService {
    public List<Ordonnance> getAll();
    public List<MedicamentDTO> getAllMedicaments();
    public MedicamentDTO getMedicamentById(int id);
    public List<MedicamentDTO> getMedicamentsByOrdonnance(int ordonnanceId);
    public void saveMedicamentToOrdonnance(int ordonnanceId, int medicamentId);
}
