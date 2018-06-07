package org.hesperides.infrastructure.springbootadmin;

import de.codecentric.boot.admin.client.config.SpringBootAdminClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import static org.hesperides.domain.framework.Profiles.SPRING_BOOT_ADMIN;

@Configuration
@Profile(SPRING_BOOT_ADMIN)
@Import(value = SpringBootAdminClientAutoConfiguration.class)
public class SpringBootAdminConfiguration {
}
