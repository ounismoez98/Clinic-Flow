package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RendezVousService implements IRendezVousService {
@Autowired
private RendezVousRepository repository;

    public List<RendezVous> getAll(){
        return  repository.findAll();

    }
    @Override
    public RendezVous updateRendezVous(int id, RendezVous newRendezvous) {
        if (repository.existsById(id)) {
            RendezVous existing = repository.findById(id).get();
            existing.setDate(newRendezvous.getDate());
            existing.setCause(newRendezvous.getCause());
            existing.setPatient(newRendezvous.getPatient());
            existing.setMedcin(newRendezvous.getMedcin());
            return repository.save(existing);
        }
        return null;
    }

    @Override
    public boolean deleteRendezVous(int id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public RendezVous getRendezVous(int id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public RendezVous addRendezVous(RendezVous rendezvous) {
        return repository.save(rendezvous);
    }
}

