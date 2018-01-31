package org.hesperides.presentation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan({"org.hesperides.presentation.controllers"})
@EnableWebMvc
public class TestAppConfig {
}
