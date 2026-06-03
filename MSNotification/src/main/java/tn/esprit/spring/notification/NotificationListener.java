package tn.esprit.spring.notification;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationListener {

    @Autowired
    private PatientClient patientClient;

    @Autowired
    private JavaMailSender mailSender;

    // 🔹 Réception
    @RabbitListener(queues = RabbitMQConfig.FACTURE_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receiveFacture(FactureDTO factureDTO) {
        System.out.println("🔔 [NOTIFICATION SERVICE] FactureDTO reçu ! ID: " + factureDTO.getId());

        try {
            // 1. Récupérer l'email du patient via Feign (avec gestion d'erreur si patient introuvable)
            DTOPatient patient = null;
            try {
                patient = patientClient.getPatientById(factureDTO.getPatientId());
            } catch (Exception fe) {
                System.err.println("⚠️ Patient " + factureDTO.getPatientId() + " non trouvé dans le service Patient. Utilisation d'un email par défaut.");
            }

            String email = (patient != null && patient.getEmail() != null) ? patient.getEmail() : "client@clinic.com";

            System.out.println("📧 Envoi email à : " + email);

            // 2. Envoyer l'email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Confirmation de Paiement - Clinic Flow");
            message.setText(
                    "Votre facture N°" + factureDTO.getId() + " de " + factureDTO.getMontant() + " DT a été payée.");

            // mailSender.send(message); // Décommenter quand le SMTP sera configuré
            System.out.println("✅ Notification traitée avec succès (Email : " + email + ").");

        } catch (Exception e) {
            System.err.println("❌ Erreur critique traitement notification : " + e.getMessage());
        }
    }
}
