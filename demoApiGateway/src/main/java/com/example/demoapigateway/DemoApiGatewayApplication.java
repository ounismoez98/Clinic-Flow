package com.example.demoapigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorDsl;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableDiscoveryClient

public class DemoApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator gatewayroutes(RouteLocatorBuilder builder)
	{
		return  builder.routes()
				.route("idroute1", r->r.path("/rendezvous/**")
						.uri("lb://MSRendezVous"))
				.route("idroute2", r->r.path("/jobs/**")
						.uri("lb://MS-job-s"))
				.route("users", r -> r.path("/users/**").uri("lb://MSUser"))
				.route("patients", r -> r.path("/patients/**").uri("lb://MSPatientMedcin"))
				.route("medecins", r -> r.path("/medecins/**").uri("lb://MSPatientMedcin"))
				.route("medicaments", r -> r.path("/medicaments/**").uri("lb://MSPharmacie"))
				.route("pharmacie-welcome-message", r -> r.path("/welcome-message").uri("lb://MSPharmacie"))
				.route("ordonnances", r -> r.path("/ordonnances/**").uri("lb://MSOrdonnance"))
				.build();

	}

}
