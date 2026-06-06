package tn.esprit.spring.notification;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    /** All notifications, newest first. */
    @GetMapping
    public List<Notification> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /** Count of unread notifications (for the bell badge). */
    @GetMapping("/unread-count")
    public long unreadCount() {
        return repository.countByReadFalse();
    }

    /** Mark a single notification as read. */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        return repository.findById(id)
                .map(n -> {
                    n.setRead(true);
                    repository.save(n);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Mark all notifications as read. */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        List<Notification> unread = repository.findAllByOrderByCreatedAtDesc();
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
        return ResponseEntity.noContent().build();
    }
}
