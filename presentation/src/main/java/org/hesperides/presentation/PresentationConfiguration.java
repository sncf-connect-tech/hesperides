package org.hesperides.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Mapper JSon principal
 * Nécessaire notamment pour le mapping des inputs (JSons vers Java)
 */
@Configuration
public class PresentationConfiguration {

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
