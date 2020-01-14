package org.hesperides;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
@EnableCaching
@Slf4j
public class HesperidesSpringApplication {

    public static void main(String[] args) {
        log.info("Program arguments: " + Arrays.toString(args));
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        ConfigurableApplicationContext ctx = SpringApplication.run(HesperidesSpringApplication.class, args);
        if (System.getenv("EXIT_AFTER_INIT") != null) {
            log.info("Immediately stopping the app:");
            System.exit(SpringApplication.exit(ctx, () -> 0));
        }
    }
}
