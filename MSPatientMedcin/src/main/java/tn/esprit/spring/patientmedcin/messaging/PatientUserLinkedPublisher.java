package tn.esprit.spring.patientmedcin.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PatientUserLinkedPublisher {

	private final RabbitTemplate rabbitTemplate;

	public PatientUserLinkedPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publish(int patientId, int userId, String patientEmail) {
		PatientUserLinkedMessage message = new PatientUserLinkedMessage(patientId, userId, patientEmail);
		rabbitTemplate.convertAndSend(
				RabbitMqClinicPatientConfig.CLINIC_PATIENT_EXCHANGE,
				RabbitMqClinicPatientConfig.PATIENT_USER_LINKED_ROUTING_KEY,
				message);
	}
}
