package com.example.mscandidat;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String ANALYSIS_QUEUE = "analysis.queue";
    public static final String ANALYSIS_EXCHANGE = "analysis.exchange";
    public static final String ANALYSIS_ROUTING_KEY = "analysis.created";

    @Bean
    public Queue analysisQueue() {
        return new Queue(ANALYSIS_QUEUE);
    }

    @Bean
    public DirectExchange analysisExchange() {
        return new DirectExchange(ANALYSIS_EXCHANGE);
    }

    @Bean
    public Binding analysisBinding(Queue analysisQueue, DirectExchange analysisExchange) {
        return BindingBuilder.bind(analysisQueue).to(analysisExchange).with(ANALYSIS_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
