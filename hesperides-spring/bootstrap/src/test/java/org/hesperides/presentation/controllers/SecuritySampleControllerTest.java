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
package org.hesperides.presentation.controllers;

import org.hesperides.HesperidesSpringApplication;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SecuritySampleController.class)
public class SecuritySampleControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void test() throws Exception {
        mvc.perform(get("/security/principal")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void test2() throws Exception {
        mvc.perform(get("/security/principal")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FOO")
    public void test3() throws Exception {
        mvc.perform(get("/security/prod")).andExpect(status().isForbidden());
    }

    @Test
    @Ignore
    @WithMockUser(roles = "TECH")
    public void test4() throws Exception {
        mvc.perform(get("/security/tech")).andExpect(status().isOk());
    }
}
