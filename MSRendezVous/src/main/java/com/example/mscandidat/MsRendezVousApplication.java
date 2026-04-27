package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MsRendezVousApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsRendezVousApplication.class, args);
	}
@Autowired
	private RendezVousRepository rendezVousRepository;
	@Bean
	ApplicationRunner init() { return
			(args) -> {
				// Vérifier si le repository est vide
				if (rendezVousRepository.count() == 0) {
					rendezVousRepository.save(new RendezVous( "2022-05-15", "Consultation générale", "Mariem Ch", "Dr. Smith"));
					rendezVousRepository.save(new RendezVous( "2026-04-16", "Suivi médical", "Sarra ab", "Dr. Johnson"));
					rendezVousRepository.save(new RendezVous( "2026-04-17", "Contrôle annuel", "Mohamed ba", "Dr. Williams"));
					rendezVousRepository.save(new RendezVous( "2026-04-18", "Examen de routine", "Maroua dh", "Dr. Brown"));
				}
// Affichage
				rendezVousRepository.findAll().forEach(System.out::println);
			};
	}

}
