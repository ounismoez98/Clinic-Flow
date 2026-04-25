package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidats")
public class CandidatRestApi {
    @RequestMapping("/hello")
    public String sayHello()
    {return "Hello FROM MS Candidat";}
    @Autowired
    private ICandidatService iCandidatService;
    @GetMapping
    public ResponseEntity<List<Candidat>> getAll()
    {

        List<Candidat> candidats = iCandidatService.getAll();
        if (candidats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(candidats);
    }
   @RequestMapping("/jobs")
    public List<JobDTO> getAllJobs()
    {return iCandidatService.getAllJobs();}


    @RequestMapping("jobs/{id}")
    public JobDTO getJobById(@PathVariable  int id)
    {return iCandidatService.getJobBYid(id);}

    @GetMapping("/{id}/favorite-jobs")
    public List<JobDTO> getFavoriteJobs(@PathVariable int id) {
        return iCandidatService.getFavoriteJobs(id);
    }

    @PostMapping("/{id}/favorite-jobs/{jobId}")
    public ResponseEntity<String> saveFavoriteJob(@PathVariable int id, @PathVariable
    int jobId) {
        JobDTO job = iCandidatService.getJobBYid(id);
        if (job != null) {
            iCandidatService.saveFavoriteJob(id, jobId);
            return ResponseEntity.status(HttpStatus.OK).body("Job saved as favorite successfully.");
        } else {
            // Gérer le cas où le job n'existe pas
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Job not found with ID: " + jobId);
        }
    }

    @Value("${welcome.message}")
    private String welcomeMessage;
    @Value("${server.port}")
    private String portserver;
    @GetMapping("/welcome")
    public String welcome() {
        return welcomeMessage +" "+portserver;
    }

}
