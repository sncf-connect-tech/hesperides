package org.hesperides.presentation.controllers;

import org.hesperides.application.Modules;
import org.hesperides.presentation.config.TestAppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(org.hesperides.presentation.controllers.ModuleController.class)
@ContextConfiguration(classes = TestAppConfig.class)
public class ModuleControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private Modules modules;

    @Test
    public void getModulesNamesTest() throws Exception {
        this.mvc.perform(get("/modules")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().is(401));
    }

}