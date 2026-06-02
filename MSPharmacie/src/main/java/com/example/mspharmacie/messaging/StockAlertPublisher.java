package com.example.mspharmacie.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockAlertPublisher {

	private final RabbitTemplate rabbitTemplate;

	public StockAlertPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publish(int medicamentId, String nomMedicament, int stockQuantity) {
		StockLowAlertMessage body = new StockLowAlertMessage(medicamentId, nomMedicament, stockQuantity);
		rabbitTemplate.convertAndSend(RabbitMqPharmacyConfig.PHARMACY_EXCHANGE, RabbitMqPharmacyConfig.STOCK_LOW_ROUTING_KEY,
				body);
	}
}
