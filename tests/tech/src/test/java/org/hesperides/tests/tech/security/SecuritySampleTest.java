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
package org.hesperides.tests.tech.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;
import static org.hesperides.domain.framework.Profiles.LDAP;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ce test fonctionne en mode MVC mocké (donc pas de server tomcat démarré)
 * mais nécessite le ldap pour fonctionner. je ne sais pas pourquoi.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({FAKE_MONGO, LDAP})
public class SecuritySampleTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    public void anonymousUserIsUnauthorized() throws Exception {
        mvc.perform(get("/security/currentUser")).andExpect(status().isUnauthorized());
        mvc.perform(get("/security/authentication")).andExpect(status().isUnauthorized());
        mvc.perform(get("/security/prod")).andExpect(status().isUnauthorized());
        mvc.perform(get("/security/tech")).andExpect(status().isUnauthorized());
        mvc.perform(get("/security/prod-or-tech")).andExpect(status().isUnauthorized());
        mvc.perform(get("/security/prod-and-tech")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void authenticatedUserIsAuthorized() throws Exception {
        mvc.perform(get("/security/currentUser")).andExpect(status().isOk());
        mvc.perform(get("/security/authentication")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void basicUserIsForbidden() throws Exception {
        mvc.perform(get("/security/prod")).andExpect(status().isForbidden());
        mvc.perform(get("/security/tech")).andExpect(status().isForbidden());
        mvc.perform(get("/security/prod-or-tech")).andExpect(status().isForbidden());
        mvc.perform(get("/security/prod-and-tech")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROD")
    public void prodUserIsAuthorized() throws Exception {
        mvc.perform(get("/security/prod")).andExpect(status().isOk());
        mvc.perform(get("/security/prod-or-tech")).andExpect(status().isOk());
        mvc.perform(get("/security/tech")).andExpect(status().isForbidden());
        mvc.perform(get("/security/prod-and-tech")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECH")
    public void techUserIsAuthorized() throws Exception {
        mvc.perform(get("/security/tech")).andExpect(status().isOk());
        mvc.perform(get("/security/prod-or-tech")).andExpect(status().isOk());
        mvc.perform(get("/security/prod")).andExpect(status().isForbidden());
        mvc.perform(get("/security/prod-and-tech")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"TECH", "PROD"})
    public void techProdUserIsAuthorized() throws Exception {
        mvc.perform(get("/security/prod-and-tech")).andExpect(status().isOk());
    }
}
