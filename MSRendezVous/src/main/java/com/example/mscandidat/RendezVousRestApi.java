package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rendezvous")
public class RendezVousRestApi {
    @RequestMapping("/hello")
    public String sayHello()
    {return "Hello FROM MS Candidat";}
    @Autowired
    private IRendezVousService iRendezVousService;

    @GetMapping("/getall")
    public ResponseEntity<List<RendezVous>> getAll()
    {

        List<RendezVous> rendezVous = iRendezVousService.getAll();
        if (rendezVous.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rendezVous);
    }

    @GetMapping("/rendezvousbyid/{id}")
    public ResponseEntity<RendezVous> getRendezVous(@PathVariable int id) {
        RendezVous rendezvous = iRendezVousService.getRendezVous(id);
        if (rendezvous == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rendezvous);
    }

    @PostMapping("/addrendezvous")
    public ResponseEntity<RendezVous> addRendezVous(@RequestBody RendezVous rendezvous) {
        RendezVous created = iRendezVousService.addRendezVous(rendezvous);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/updaterendezvous/{id}")
    public ResponseEntity<RendezVous> updateRendezVous(@PathVariable int id, @RequestBody RendezVous rendezvous) {
        RendezVous updated = iRendezVousService.updateRendezVous(id, rendezvous);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/deleterendezvous/{id}")
    public ResponseEntity<Void> deleteRendezVous(@PathVariable int id) {
        boolean deleted = iRendezVousService.deleteRendezVous(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
