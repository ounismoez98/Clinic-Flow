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
 * Déclare l’exchange, la queue et la liaison côté producteur également.
 * Sinon, sans MSUser démarré avant la publication, aucune queue n’est liée à
 * {@code clinic.patient.exchange} + {@code patient.user.linked} → messages non routés, perdus sans erreur HTTP.
 */
@Configuration
public class RabbitMqClinicPatientConfig {

	public static final String CLINIC_PATIENT_EXCHANGE = "clinic.patient.exchange";
	public static final String PATIENT_USER_LINKED_ROUTING_KEY = "patient.user.linked";
	public static final String PATIENT_USER_LINKED_QUEUE = "patient.user.linked.queue";

	@Bean
	public DirectExchange clinicPatientExchange() {
		return new DirectExchange(CLINIC_PATIENT_EXCHANGE);
	}

	@Bean
	public Queue patientUserLinkedQueue() {
		return new Queue(PATIENT_USER_LINKED_QUEUE);
	}

	@Bean
	public Binding patientUserLinkedBinding(Queue patientUserLinkedQueue, DirectExchange clinicPatientExchange) {
		return BindingBuilder.bind(patientUserLinkedQueue)
				.to(clinicPatientExchange)
				.with(PATIENT_USER_LINKED_ROUTING_KEY);
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
