package com.example.msordonnance;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMessageProducer {
	private static final String ANALYSIS_EXCHANGE = AnalysisRabbitMqTopologyConfig.ANALYSIS_EXCHANGE;
	private static final String ANALYSIS_ROUTING_KEY = AnalysisRabbitMqTopologyConfig.ANALYSIS_ROUTING_KEY;

    private final RabbitTemplate rabbitTemplate;

    public AnalysisMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAnalysisRequest(AnalysisRequestMessage message) {
        rabbitTemplate.convertAndSend(ANALYSIS_EXCHANGE, ANALYSIS_ROUTING_KEY, message);
    }
}
