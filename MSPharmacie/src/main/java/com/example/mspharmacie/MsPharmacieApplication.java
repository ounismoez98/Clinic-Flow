package com.example.mspharmacie;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.example.mspharmacie.ai.GeminiProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties(GeminiProperties.class)
public class MsPharmacieApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsPharmacieApplication.class, args);
	}

	@Autowired
	private MedicamentRepository medicamentRepository;

	@Bean
	ApplicationRunner seedCatalogue() {
		return args -> {
			if (medicamentRepository.count() == 0) {
				medicamentRepository.save(new Medicament("Paracétamol 500mg", true, 120, new BigDecimal("1.80")));
				medicamentRepository.save(new Medicament("Ibuprofène 400mg", true, 80, new BigDecimal("3.50")));
				medicamentRepository.save(new Medicament("Amoxicilline", true, 40, new BigDecimal("12.00")));
				medicamentRepository.save(new Medicament("Vitamine D", false, 200, new BigDecimal("6.25")));
				medicamentRepository.save(new Medicament("Sirop toux", true, 15, new BigDecimal("4.90")));
			}
		};
	}
}
