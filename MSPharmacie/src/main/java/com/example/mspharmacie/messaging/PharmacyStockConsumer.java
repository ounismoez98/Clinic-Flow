package com.example.mspharmacie.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.mspharmacie.IMedicamentService;

@Component
public class PharmacyStockConsumer {

	private static final Logger log = LoggerFactory.getLogger(PharmacyStockConsumer.class);

	private final IMedicamentService medicamentService;

	public PharmacyStockConsumer(IMedicamentService medicamentService) {
		this.medicamentService = medicamentService;
	}

	@RabbitListener(queues = RabbitMqPharmacyConfig.ORDONNANCE_MEDICAMENT_QUEUE)
	public void onOrdonnanceMedicamentAdded(OrdonnanceMedicamentAddedMessage message) {
		log.info(
				"MQ — médicament ajouté à ordonnance {} : medicamentId={}, qty={}",
				message.getOrdonnanceId(),
				message.getMedicamentId(),
				message.getQuantity());
		medicamentService.applyOrdonnanceStockConsumption(message.getMedicamentId(), message.getQuantity());
	}
}
