package tn.esprit.spring.patientmedcin.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes patient events asynchronously (fire-and-forget). The caller does
 * NOT wait for MSNotification to process anything — it just drops the message
 * on the exchange and returns immediately.
 */
@Component
public class PatientEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	public PatientEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publish(String eventType, int patientId, String patientName, String patientEmail) {
		PatientEventMessage message =
				new PatientEventMessage(eventType, patientId, patientName, patientEmail);
		rabbitTemplate.convertAndSend(
				RabbitMqClinicPatientConfig.CLINIC_PATIENT_EXCHANGE,
				RabbitMqClinicPatientConfig.PATIENT_EVENT_ROUTING_KEY,
				message);
	}
}
