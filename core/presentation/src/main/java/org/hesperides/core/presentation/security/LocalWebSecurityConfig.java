package org.hesperides.core.presentation.security;

import org.hesperides.core.domain.security.entities.springauthorities.GlobalRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import static org.hesperides.commons.SpringProfiles.NOLDAP;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@Profile(NOLDAP)
public class LocalWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("#{'${hesperides.security.auth-whitelist}'.split('\\|')}")
    String[] authWhitelist;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(STATELESS);
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers(authWhitelist).permitAll()
                .anyRequest().fullyAuthenticated()
                .and().httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("tech").password("{noop}password").authorities(GlobalRole.GLOBAL_IS_TECH)
                .and().withUser("prod").password("{noop}password").authorities(GlobalRole.GLOBAL_IS_PROD)
                .and().withUser("user").password("{noop}password").roles("USER");
    }
}
