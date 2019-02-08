package org.hesperides;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HesperidesSpringApplication {

    public static void main(String[] args) {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        SpringApplication.run(HesperidesSpringApplication.class, args);
    }
}
