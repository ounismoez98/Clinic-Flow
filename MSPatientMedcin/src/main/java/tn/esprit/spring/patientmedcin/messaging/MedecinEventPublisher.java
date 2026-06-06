package tn.esprit.spring.patientmedcin.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes medecin events asynchronously (fire-and-forget). The caller does
 * NOT wait for MSNotification — it drops the message on the exchange and returns.
 */
@Component
public class MedecinEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	public MedecinEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publish(String eventType, int medecinId, String medecinName,
	                    String specialite, String email) {
		MedecinEventMessage message =
				new MedecinEventMessage(eventType, medecinId, medecinName, specialite, email);
		rabbitTemplate.convertAndSend(
				RabbitMqClinicPatientConfig.CLINIC_PATIENT_EXCHANGE,
				RabbitMqClinicPatientConfig.MEDECIN_EVENT_ROUTING_KEY,
				message);
	}
}
