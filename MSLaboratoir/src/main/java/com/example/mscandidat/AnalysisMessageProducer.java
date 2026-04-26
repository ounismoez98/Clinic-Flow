package com.example.mscandidat;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMessageProducer {
    private final RabbitTemplate rabbitTemplate;

    public AnalysisMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAnalysisRequest(AnalysisRequestMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.ANALYSIS_EXCHANGE,
                RabbitMqConfig.ANALYSIS_ROUTING_KEY,
                message
        );
    }
}
