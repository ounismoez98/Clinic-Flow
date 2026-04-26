package com.example.msordonnance;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMessageProducer {
    private static final String ANALYSIS_EXCHANGE = "analysis.exchange";
    private static final String ANALYSIS_ROUTING_KEY = "analysis.created";

    private final RabbitTemplate rabbitTemplate;

    public AnalysisMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAnalysisRequest(AnalysisRequestMessage message) {
        rabbitTemplate.convertAndSend(ANALYSIS_EXCHANGE, ANALYSIS_ROUTING_KEY, message);
    }
}
