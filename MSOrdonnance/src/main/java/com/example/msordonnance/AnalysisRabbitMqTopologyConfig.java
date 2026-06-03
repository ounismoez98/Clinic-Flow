package com.example.msordonnance;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares analysis exchanges/queues/bindings here so MSOrdonnance can start without MSLaboratoir.
 * Matches topology in {@code MSLaboratoir}'s {@code RabbitMqConfig} (idempotent on RabbitMQ).
 */
@Configuration
public class AnalysisRabbitMqTopologyConfig {

	public static final String ANALYSIS_QUEUE = "analysis.queue";
	public static final String ANALYSIS_EXCHANGE = "analysis.exchange";
	public static final String ANALYSIS_ROUTING_KEY = "analysis.created";

	public static final String ANALYSIS_COMPLETED_QUEUE = "analysis.completed.queue";
	public static final String ANALYSIS_COMPLETED_EXCHANGE = "analysis.completed.exchange";
	public static final String ANALYSIS_COMPLETED_ROUTING_KEY = "analysis.completed";

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
	public Queue analysisCompletedQueue() {
		return new Queue(ANALYSIS_COMPLETED_QUEUE);
	}

	@Bean
	public DirectExchange analysisCompletedExchange() {
		return new DirectExchange(ANALYSIS_COMPLETED_EXCHANGE);
	}

	@Bean
	public Binding analysisCompletedBinding(Queue analysisCompletedQueue, DirectExchange analysisCompletedExchange) {
		return BindingBuilder.bind(analysisCompletedQueue).to(analysisCompletedExchange).with(ANALYSIS_COMPLETED_ROUTING_KEY);
	}
}
