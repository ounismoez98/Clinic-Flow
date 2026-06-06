package tn.esprit.spring.notification;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * Reacts ASYNCHRONOUSLY to medecin events published by MSPatientMedcin.
 * Decoupled side-effect: persists a notification when a doctor is created.
 */
@Service
public class MedecinEventListener {

    private final NotificationRepository notificationRepository;

    public MedecinEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.MEDECIN_EVENT_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void onMedecinEvent(MedecinEventMessage event) {
        String message = "New doctor joined: " + event.getMedecinName()
                + (event.getSpecialite() != null ? " (" + event.getSpecialite() + ")" : "");

        notificationRepository.save(
                new Notification(event.getEventType(), message, event.getEmail()));

        System.out.println("🔔 [NOTIFICATION] saved medecin '" + event.getEventType()
                + "' for medecinId=" + event.getMedecinId() + " -> " + message);
    }
}
