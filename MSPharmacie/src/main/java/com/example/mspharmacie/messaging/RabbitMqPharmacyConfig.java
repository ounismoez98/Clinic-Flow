package com.example.mspharmacie.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqPharmacyConfig {

	public static final String PHARMACY_EXCHANGE = "clinic.pharmacy.exchange";
	public static final String ORDONNANCE_MEDICAMENT_ADDED_ROUTING_KEY = "ordonnance.medicament.added";
	public static final String ORDONNANCE_MEDICAMENT_QUEUE = "pharmacy.ordonnance.medicament.queue";
	public static final String STOCK_LOW_ROUTING_KEY = "pharmacy.stock.low";

	@Bean
	public DirectExchange pharmacyExchange() {
		return new DirectExchange(PHARMACY_EXCHANGE);
	}

	@Bean
	public Queue ordonnanceMedicamentQueue() {
		return new Queue(ORDONNANCE_MEDICAMENT_QUEUE);
	}

	@Bean
	public Binding ordonnanceMedicamentBinding(Queue ordonnanceMedicamentQueue, DirectExchange pharmacyExchange) {
		return BindingBuilder.bind(ordonnanceMedicamentQueue).to(pharmacyExchange).with(ORDONNANCE_MEDICAMENT_ADDED_ROUTING_KEY);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter pharmacyJacksonMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(pharmacyJacksonMessageConverter);
		return factory;
	}

	@Bean
	public Jackson2JsonMessageConverter pharmacyJacksonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplateCustomizer pharmacyRabbitTemplateCustomizer(Jackson2JsonMessageConverter pharmacyJacksonMessageConverter) {
		return template -> template.setMessageConverter(pharmacyJacksonMessageConverter);
	}
}
