package tn.esprit.spring.patientmedcin.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the exchange/queue/binding for the patient.event async flow
 * (MSPatientMedcin -> MSNotification) on the producer side too, so messages
 * route even if MSNotification starts later.
 *
 * (The old patient.user.linked async flow was removed: the patient<->user link
 * is now set synchronously via a Feign call to MSUser.)
 */
@Configuration
public class RabbitMqClinicPatientConfig {

	public static final String CLINIC_PATIENT_EXCHANGE = "clinic.patient.exchange";

	// Async "a patient event happened" -> MSNotification reacts.
	public static final String PATIENT_EVENT_ROUTING_KEY = "patient.event";
	public static final String PATIENT_EVENT_QUEUE = "patient.event.queue";

	// Async "a medecin event happened" -> MSNotification reacts.
	// Same exchange, different routing key -> a separate queue/consumer.
	public static final String MEDECIN_EVENT_ROUTING_KEY = "medecin.event";
	public static final String MEDECIN_EVENT_QUEUE = "medecin.event.queue";

	@Bean
	public DirectExchange clinicPatientExchange() {
		return new DirectExchange(CLINIC_PATIENT_EXCHANGE);
	}

	@Bean
	public Queue patientEventQueue() {
		return new Queue(PATIENT_EVENT_QUEUE);
	}

	@Bean
	public Binding patientEventBinding(Queue patientEventQueue, DirectExchange clinicPatientExchange) {
		return BindingBuilder.bind(patientEventQueue)
				.to(clinicPatientExchange)
				.with(PATIENT_EVENT_ROUTING_KEY);
	}

	@Bean
	public Queue medecinEventQueue() {
		return new Queue(MEDECIN_EVENT_QUEUE);
	}

	@Bean
	public Binding medecinEventBinding(Queue medecinEventQueue, DirectExchange clinicPatientExchange) {
		return BindingBuilder.bind(medecinEventQueue)
				.to(clinicPatientExchange)
				.with(MEDECIN_EVENT_ROUTING_KEY);
	}

	@Bean
	public Jackson2JsonMessageConverter patientMedcinJacksonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
			Jackson2JsonMessageConverter patientMedcinJacksonMessageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(patientMedcinJacksonMessageConverter);
		return template;
	}
}
