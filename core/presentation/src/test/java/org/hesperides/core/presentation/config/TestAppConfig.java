package org.hesperides.core.presentation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan({"org.hesperides.core.presentation.controllers","org.hesperides.core.presentation.swagger"})
@EnableWebMvc
public class TestAppConfig {
}
