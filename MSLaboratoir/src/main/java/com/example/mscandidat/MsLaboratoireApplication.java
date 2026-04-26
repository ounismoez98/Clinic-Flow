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
public class MsLaboratoireApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsLaboratoireApplication.class, args);
    }

    @Autowired
    private LaboratoireRepository laboratoireRepository;

    @Bean
    ApplicationRunner init() {
        return (args) -> {
            if (laboratoireRepository.count() == 0) {
                laboratoireRepository.save(new Laboratoire("Lab Centrale", "Tunis", "20123456", "lab1@esprit.tn"));
                laboratoireRepository.save(new Laboratoire("Lab Nord", "Ariana", "22123456", "lab2@esprit.tn"));
            }
            laboratoireRepository.findAll().forEach(System.out::println);
        };
    }
}
