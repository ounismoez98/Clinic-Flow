package com.example.mscandidat;

import java.util.List;

public interface ICandidatService {

    public List<Candidat> getAll();
    public List<JobDTO> getAllJobs();
    public JobDTO getJobBYid(int id );

    public List<JobDTO> getFavoriteJobs(int candidateId);
    public void saveFavoriteJob(int candidateId, int jobId);
}
