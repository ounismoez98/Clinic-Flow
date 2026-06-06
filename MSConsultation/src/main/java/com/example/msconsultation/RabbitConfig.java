package com.example.msconsultation;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String CONSULTATION_FINALIZED_QUEUE = "consultation_finalized_queue";

    @Bean
    public Queue consultationFinalizedQueue() {
        return new Queue(CONSULTATION_FINALIZED_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}
