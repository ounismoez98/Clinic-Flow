package com.example.msordonnance;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AnalysisCompletedConsumer {
    @RabbitListener(queues = "analysis.completed.queue")
    public void handleCompleted(AnalysisCompletedMessage message) {
        System.out.println("Received analysis completed: analysisId=" + message.getAnalysisId()
                + ", ordonnanceId=" + message.getOrdonnanceId()
                + ", patientId=" + message.getPatientId()
                + ", medcinId=" + message.getMedcinId()
                + ", result=" + message.getResult());
    }
}
