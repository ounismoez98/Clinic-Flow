package com.example.mscandidat;

import java.util.List;

public interface ILaboratoireService {

    List<Laboratoire> getAll();
    List<JobDTO> getAllJobs();
    JobDTO getJobBYid(int id);

    List<JobDTO> getFavoriteJobs(int laboratoireId);
    void saveFavoriteJob(int laboratoireId, int jobId);
}
