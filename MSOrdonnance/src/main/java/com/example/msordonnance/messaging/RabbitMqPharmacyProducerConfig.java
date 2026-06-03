package com.example.msordonnance.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqPharmacyProducerConfig {

	public static final String PHARMACY_EXCHANGE = "clinic.pharmacy.exchange";
	public static final String ORDONNANCE_MEDICAMENT_ADDED_ROUTING_KEY = "ordonnance.medicament.added";

	@Bean
	public DirectExchange pharmacyExchangeForProducer() {
		return new DirectExchange(PHARMACY_EXCHANGE);
	}

	@Bean
	public RabbitTemplateCustomizer ordonnancePharmacyJacksonCustomizer(Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
		return template -> template.setMessageConverter(jackson2JsonMessageConverter);
	}

	@Bean
	public Jackson2JsonMessageConverter ordonnanceJackson2JsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
