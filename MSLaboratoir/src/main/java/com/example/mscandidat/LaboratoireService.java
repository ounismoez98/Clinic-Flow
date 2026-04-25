package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LaboratoireService implements ILaboratoireService {
    @Autowired
    private LaboratoireRepository laboratoireRepository;
    @Autowired
    private JobClient jobClient;

    public List<Laboratoire> getAll() {
        return laboratoireRepository.findAll();
    }

    public List<JobDTO> getAllJobs() {
        return jobClient.getAll();
    }

    public JobDTO getJobBYid(int id) {
        return jobClient.getJobById(id);
    }

    public List<JobDTO> getFavoriteJobs(int laboratoireId) {
        Laboratoire laboratoire = laboratoireRepository.findById(laboratoireId).get();
        return laboratoire.getFavoriteJobs().stream()
                .map(jobClient::getJobById)
                .collect(Collectors.toList());
    }

    public void saveFavoriteJob(int laboratoireId, int jobId) {
        Laboratoire laboratoire = laboratoireRepository.findById(laboratoireId).get();
        laboratoire.getFavoriteJobs().add(jobId);
        laboratoireRepository.save(laboratoire);
    }
}
