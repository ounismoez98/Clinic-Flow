package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LaboratoireService implements ILaboratoireService {
    @Autowired
    private LaboratoireRepository laboratoireRepository;

    public List<Laboratoire> getAll() {
        return laboratoireRepository.findAll();
    }
}
