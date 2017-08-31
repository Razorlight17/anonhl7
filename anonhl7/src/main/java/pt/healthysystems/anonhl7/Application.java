package pt.healthysystems.anonhl7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"pt.healthysystems.anonhl7"}) 
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

