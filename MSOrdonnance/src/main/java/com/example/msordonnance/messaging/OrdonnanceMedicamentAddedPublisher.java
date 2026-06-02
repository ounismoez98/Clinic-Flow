package com.example.msordonnance.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrdonnanceMedicamentAddedPublisher {

	private final RabbitTemplate rabbitTemplate;

	public OrdonnanceMedicamentAddedPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publish(int ordonnanceId, int medicamentId, int quantity) {
		OrdonnanceMedicamentAddedMessage message = new OrdonnanceMedicamentAddedMessage(ordonnanceId, medicamentId, quantity);
		rabbitTemplate.convertAndSend(RabbitMqPharmacyProducerConfig.PHARMACY_EXCHANGE,
				RabbitMqPharmacyProducerConfig.ORDONNANCE_MEDICAMENT_ADDED_ROUTING_KEY, message);
	}
}
