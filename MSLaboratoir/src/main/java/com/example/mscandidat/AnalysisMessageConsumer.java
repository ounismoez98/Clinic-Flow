package com.example.mscandidat;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMessageConsumer {

    @RabbitListener(queues = RabbitMqConfig.ANALYSIS_QUEUE)
    public void handleAnalysisRequest(AnalysisRequestMessage message) {
        System.out.println("Received analysis request: type=" + message.getType()
                + ", patientId=" + message.getPatientId()
                + ", medcinId=" + message.getMedcinId()
                + ", laboratoireId=" + message.getLaboratoireId()
                + ", status=" + message.getStatus());
    }
}
