package tn.esprit.spring.user.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tn.esprit.spring.user.IUserService;

@Component
public class PatientUserLinkedConsumer {

	private static final Logger log = LoggerFactory.getLogger(PatientUserLinkedConsumer.class);

	private final IUserService userService;

	public PatientUserLinkedConsumer(IUserService userService) {
		this.userService = userService;
	}

	@RabbitListener(queues = RabbitMqClinicPatientConfig.PATIENT_USER_LINKED_QUEUE)
	public void onPatientLinkedToUser(PatientUserLinkedMessage message) {
		log.info("RabbitMQ event received: linking patientId={} to userId={}", message.getPatientId(), message.getUserId());
		userService.linkPatient(message.getUserId(), message.getPatientId());
		log.info("User id={} successfully linked to patientId={}", message.getUserId(), message.getPatientId());
	}
}
