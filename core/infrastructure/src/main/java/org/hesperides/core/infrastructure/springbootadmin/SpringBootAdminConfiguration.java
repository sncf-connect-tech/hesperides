package org.hesperides.core.infrastructure.springbootadmin;

import de.codecentric.boot.admin.client.config.SpringBootAdminClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static org.hesperides.commons.spring.SpringProfiles.SPRING_BOOT_ADMIN;

@Configuration
@Profile(SPRING_BOOT_ADMIN)
@Import(value = SpringBootAdminClientAutoConfiguration.class)
public class SpringBootAdminConfiguration {
}
