package com.example.mscandidat;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalysisCompletedProducer {
    private final RabbitTemplate rabbitTemplate;

    public AnalysisCompletedProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendCompleted(AnalysisCompletedMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.ANALYSIS_COMPLETED_EXCHANGE,
                RabbitMqConfig.ANALYSIS_COMPLETED_ROUTING_KEY,
                message
        );
    }
}
