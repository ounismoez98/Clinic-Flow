package tn.esprit.spring.patientmedcin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedecinService implements IMedecinService {

    private final MedecinRepository medecinRepository;

    public MedecinService(MedecinRepository medecinRepository) {
        this.medecinRepository = medecinRepository;
    }

    @Override
    public List<Medecin> getAll() {
        return medecinRepository.findAll();
    }

    @Override
    @Transactional
    public Medecin create(Medecin medecin) {
        return medecinRepository.save(medecin);
    }

    @Override
    @Transactional
    public Medecin update(int id, Medecin medecin) {
        return medecinRepository.findById(id)
                .map(existing -> {
                    existing.setNom(medecin.getNom());
                    existing.setPrenom(medecin.getPrenom());
                    existing.setEmail(medecin.getEmail());
                    existing.setSpecialite(medecin.getSpecialite());
                    return medecinRepository.save(existing);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        if (!medecinRepository.existsById(id)) {
            return false;
        }
        medecinRepository.deleteById(id);
        return true;
    }

    @Override
    public Medecin getById(int id) {
        return medecinRepository.findById(id).orElse(null);
    }
}
