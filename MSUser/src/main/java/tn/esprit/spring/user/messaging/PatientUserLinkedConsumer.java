package tn.esprit.spring.user.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PatientUserLinkedConsumer {

	private static final Logger log = LoggerFactory.getLogger(PatientUserLinkedConsumer.class);

	@RabbitListener(queues = RabbitMqClinicPatientConfig.PATIENT_USER_LINKED_QUEUE)
	public void onPatientLinkedToUser(PatientUserLinkedMessage message) {
		log.info(
				"Asynchronous RabbitMQ notification: patientId={} is linked to userId={} (patient email hint: {}).",
				message.getPatientId(),
				message.getUserId(),
				message.getPatientEmail());
	}
}
