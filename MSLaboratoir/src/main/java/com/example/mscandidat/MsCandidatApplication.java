package com.example.mscandidat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class MsCandidatApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCandidatApplication.class, args);
	}
@Autowired
	private CandidatRepository candidatRepository;
	@Bean
	ApplicationRunner init() { return
			(args) -> {
				// Vérifier si le repository est vide
				if (candidatRepository.count() == 0) {
					candidatRepository.save(new Candidat("Mariem", "Ch", "ma@esprit.tn"));
					candidatRepository.save(new Candidat("Sarra", "ab", "sa@esprit.tn"));
					candidatRepository.save(new Candidat("Mohamed", "ba", "mo@esprit.tn"));
					candidatRepository.save(new Candidat("Maroua", "dh", "maroua@esprit.tn"));
				}
// Affichage
				candidatRepository.findAll().forEach(System.out::println);
			};
	}

}
