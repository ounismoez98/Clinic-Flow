package tn.esprit.spring.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Autowired
    private UserRepository userRepository;

    @Bean
    ApplicationRunner init() {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("admin", "admin123", "admin@clinic.tn", Role.ADMIN));
                userRepository.save(new User("samir.benali", "pass123", "samir.benali@clinic.tn", Role.MEDECIN));
                userRepository.save(new User("leila.trabelsi", "pass123", "leila.trabelsi@clinic.tn", Role.MEDECIN));
                userRepository.save(new User("mariem.ch", "pass123", "ma@patient.tn", Role.PATIENT));
                userRepository.save(new User("sarra.ab", "pass123", "sa@patient.tn", Role.PATIENT));
            }
        };
    }
}
