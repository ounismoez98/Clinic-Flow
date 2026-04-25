package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidatService implements ICandidatService{
@Autowired
private CandidatRepository candidatRepository;
@Autowired
private JobClient jobClient;

    public List<Candidat> getAll(){
        return  candidatRepository.findAll();

    }
    public List<JobDTO> getAllJobs(){
return  jobClient.getAll();
    }

    public JobDTO getJobBYid(int id )
    {
        return  jobClient.getJobById(id);
    }

    public List<JobDTO> getFavoriteJobs(int candidateId) {
        Candidat candidate = candidatRepository.findById(candidateId).get();
        return candidate.getFavoriteJobs().stream()
                .map(jobClient::getJobById)
                .collect(Collectors.toList());
    }
    public void saveFavoriteJob(int candidateId, int jobId) {
        Candidat candidate = candidatRepository.findById(candidateId).get();
        candidate.getFavoriteJobs().add(jobId);
        candidatRepository.save(candidate);
    }
}
