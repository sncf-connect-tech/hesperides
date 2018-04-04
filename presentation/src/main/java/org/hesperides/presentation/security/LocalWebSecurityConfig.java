package org.hesperides.presentation.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Profile("noldap")
public class LocalWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String PATHWORD = "password";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().fullyAuthenticated()
                .and().httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("tech").password(PATHWORD).roles("TECH")
                .and().withUser("prod").password(PATHWORD).roles("PROD")
                .and().withUser("user").password(PATHWORD).roles("USER");
    }
}
