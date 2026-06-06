package tn.esprit.spring.notification;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Reacts ASYNCHRONOUSLY to patient events published by MSPatientMedcin.
 * The patient service already returned its HTTP response by the time we run here;
 * this is a pure side-effect (persist a notification), decoupled from the request.
 */
@Service
public class PatientEventListener {

    private final NotificationRepository notificationRepository;

    public PatientEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.PATIENT_EVENT_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void onPatientEvent(PatientEventMessage event) {
        String message = switch (event.getEventType() == null ? "" : event.getEventType()) {
            case "CREATED" -> "Welcome to Clinic Flow, " + event.getPatientName() + "!";
            case "ADMITTED" -> "Patient " + event.getPatientName() + " has been admitted.";
            default -> "Patient event: " + event.getEventType();
        };

        // Persist so the frontend can fetch it via GET /notifications.
        notificationRepository.save(
                new Notification(event.getEventType(), message, event.getPatientEmail()));

        System.out.println("🔔 [NOTIFICATION] saved '" + event.getEventType()
                + "' for patientId=" + event.getPatientId()
                + " (" + event.getPatientEmail() + ") -> " + message);
    }
}
