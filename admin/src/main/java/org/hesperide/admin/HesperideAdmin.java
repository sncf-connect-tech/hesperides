package org.hesperide.admin;

import de.codecentric.boot.admin.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@EnableAdminServer
@Configuration
@SpringBootApplication
public class HesperideAdmin {

    public static void main(String[] args) {
        SpringApplication.run(HesperideAdmin.class, args);
    }
}
