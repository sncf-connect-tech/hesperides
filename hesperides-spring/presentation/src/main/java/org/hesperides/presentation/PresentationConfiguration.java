package org.hesperides.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PresentationConfiguration {

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
