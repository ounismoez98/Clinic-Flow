package tn.esprit.spring.patientmedcin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PatientMedcinApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientMedcinApplication.class, args);
    }

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private MedecinRepository medecinRepository;

    @Bean
    ApplicationRunner init() {
        return args -> {
            if (medecinRepository.count() == 0) {
                medecinRepository.save(new Medecin("Ben Ali", "Samir", "samir.benali@clinic.tn", "Cardiologie"));
                medecinRepository.save(new Medecin("Trabelsi", "Leila", "leila.trabelsi@clinic.tn", "Pediatrie"));
                medecinRepository.save(new Medecin("Guesmi", "Karim", "karim.guesmi@clinic.tn", "Medecine generale"));
            }
            if (patientRepository.count() == 0) {
                patientRepository.save(new Patient("Mariem", "Ch", "ma@patient.tn"));
                patientRepository.save(new Patient("Sarra", "Ab", "sa@patient.tn"));
                patientRepository.save(new Patient("Mohamed", "Ba", "mo@patient.tn"));
                patientRepository.save(new Patient("Maroua", "Dh", "maroua@patient.tn"));
            }
        };
    }
}
