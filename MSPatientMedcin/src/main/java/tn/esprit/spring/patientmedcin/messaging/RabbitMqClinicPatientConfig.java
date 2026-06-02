package tn.esprit.spring.patientmedcin.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqClinicPatientConfig {

	public static final String CLINIC_PATIENT_EXCHANGE = "clinic.patient.exchange";
	public static final String PATIENT_USER_LINKED_ROUTING_KEY = "patient.user.linked";

	@Bean
	public DirectExchange clinicPatientExchange() {
		return new DirectExchange(CLINIC_PATIENT_EXCHANGE);
	}

	@Bean
	public Jackson2JsonMessageConverter patientMedcinJacksonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
