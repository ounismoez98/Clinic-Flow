package com.example.msordonnance;

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
public class MsOrdonnanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsOrdonnanceApplication.class, args);
    }

    @Autowired
    private OrdonnanceRepository ordonnanceRepository;

    @Bean
    ApplicationRunner init() {
        return (args) -> {
            if (ordonnanceRepository.count() == 0) {
                ordonnanceRepository.save(new Ordonnance("Mariem", "Ch", "ma@esprit.tn"));
                ordonnanceRepository.save(new Ordonnance("Sarra", "ab", "sa@esprit.tn"));
                ordonnanceRepository.save(new Ordonnance("Mohamed", "ba", "mo@esprit.tn"));
                ordonnanceRepository.save(new Ordonnance("Maroua", "dh", "maroua@esprit.tn"));
            }
            ordonnanceRepository.findAll().forEach(o -> System.out.println(o.getNom() + " " + o.getPrenom()));
        };
    }
}
