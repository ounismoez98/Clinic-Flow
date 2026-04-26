package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Autowired
    private AnalysisMessageProducer analysisMessageProducer;

    @GetMapping
    public ResponseEntity<List<Laboratoire>> getAll() {
        List<Laboratoire> laboratoires = iLaboratoireService.getAll();
        if (laboratoires.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(laboratoires);
    }

    @PostMapping("/analysis-requests")
    public ResponseEntity<String> publishAnalysisRequest(@RequestBody AnalysisRequestMessage message) {
        analysisMessageProducer.sendAnalysisRequest(message);
        return ResponseEntity.ok("Analysis request sent.");
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
