package tn.esprit.spring.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Newest first, for the bell dropdown. */
    List<Notification> findAllByOrderByCreatedAtDesc();

    long countByReadFalse();
}
