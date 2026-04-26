package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/laboratoires")
public class LaboratoireRestApi {
    @RequestMapping("/hello")
    public String sayHello() {
        return "Hello FROM MS Laboratoire";
    }

    @Autowired
    private ILaboratoireService iLaboratoireService;

    @GetMapping
    public ResponseEntity<List<Laboratoire>> getAll() {
        List<Laboratoire> laboratoires = iLaboratoireService.getAll();
        if (laboratoires.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(laboratoires);
    }

    @RequestMapping("/jobs")
    public List<JobDTO> getAllJobs() {
        return iLaboratoireService.getAllJobs();
    }

    @RequestMapping("jobs/{id}")
    public JobDTO getJobById(@PathVariable int id) {
        return iLaboratoireService.getJobBYid(id);
    }

    @GetMapping("/{id}/favorite-jobs")
    public List<JobDTO> getFavoriteJobs(@PathVariable int id) {
        return iLaboratoireService.getFavoriteJobs(id);
    }

    @PostMapping("/{id}/favorite-jobs/{jobId}")
    public ResponseEntity<String> saveFavoriteJob(@PathVariable int id, @PathVariable int jobId) {
        JobDTO job = iLaboratoireService.getJobBYid(id);
        if (job != null) {
            iLaboratoireService.saveFavoriteJob(id, jobId);
            return ResponseEntity.status(HttpStatus.OK).body("Job saved as favorite successfully.");
        } else {
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
        return welcomeMessage + " " + portserver;
    }
}
