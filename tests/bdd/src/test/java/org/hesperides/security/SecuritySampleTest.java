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
package org.hesperides.security;

import org.hesperides.HesperidesSpringApplication;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
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
        mvc.perform(MockMvcRequestBuilders.get("/security/principal")).andExpect(MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(MockMvcRequestBuilders.get("/security/authentication")).andExpect(MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod")).andExpect(MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(MockMvcRequestBuilders.get("/security/tech")).andExpect(MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-or-tech")).andExpect(MockMvcResultMatchers.status().isUnauthorized());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-and-tech")).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void authenticatedUserIsAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/security/principal")).andExpect(MockMvcResultMatchers.status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/security/authentication")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser
    public void basicUserIsForbidden() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/security/prod")).andExpect(MockMvcResultMatchers.status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.get("/security/tech")).andExpect(MockMvcResultMatchers.status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-or-tech")).andExpect(MockMvcResultMatchers.status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-and-tech")).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROD")
    public void prodUserIsAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/security/prod")).andExpect(MockMvcResultMatchers.status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-or-tech")).andExpect(MockMvcResultMatchers.status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/security/tech")).andExpect(MockMvcResultMatchers.status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-and-tech")).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECH")
    public void techUserIsAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/security/tech")).andExpect(MockMvcResultMatchers.status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-or-tech")).andExpect(MockMvcResultMatchers.status().isOk());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod")).andExpect(MockMvcResultMatchers.status().isForbidden());
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-and-tech")).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"TECH", "PROD"})
    public void techProdUserIsAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/security/prod-and-tech")).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
