/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.vsct.dt.hesperides.spring.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.LdapShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${ldap.urls}")
    private String ldapUrls;

    @Value("${ldap.base.dn}")
    private String ldapBaseDn;

    @Value("${ldap.username}")
    private String ldapSecurityPrincipal;

    @Value("${ldap.password}")
    private String ldapPrincipalPassword;

    @Value("${ldap.user.dn.pattern}")
    private String ldapUserDnPattern;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().fullyAuthenticated()
                .and().httpBasic();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {

        /*
        server = ldapUri,
                rootDN = rootDN,
                userSearchBase = "",
                userSearch = "sAMAccountName={0}",
                groupSearchBase = "",
                groupSearchFilter = "",
                groupMembershipStrategy = null,
                managerDN = "CN=admjenkins,OU=Service_Accounts,OU=ARCHITECTURE,DC=groupevsc,DC=com",
                managerPasswordSecret = "",
                inhibitInferRootDN = false,
                disableMailAddressResolver = false,
                cache = null);
         */

        auth.ldapAuthentication()
                .userDnPatterns("dc=groupevsc,dc=com")
                .contextSource().url("ldap://adumar.groupevsc.com")
                .managerDn("CN=admjenkins,OU=Service_Accounts,OU=ARCHITECTURE,DC=groupevsc,DC=com")
                .and()
                .userSearchFilter("sAMAccountName={0}");
//        auth.ldapAuthentication().userSearchBase("dc=groupevsc,dc=com").contextSource().url();
//                .userDnPatterns("sAMAccountName={0}")
//                .userSearchBase("dc=groupevsc,dc=com")
////                .groupSearchBase("ou=ARCHITECTURE,dc=groupevsc,dc=com")
//                .contextSource()
//                .url("ldap://adumar.groupevsc.com:389")
//                .and()
//                .passwordCompare()
//                .passwordEncoder(new LdapShaPasswordEncoder())
//                .passwordAttribute("userPassword");
    }
}
