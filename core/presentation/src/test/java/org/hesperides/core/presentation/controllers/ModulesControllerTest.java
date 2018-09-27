package org.hesperides.core.presentation.controllers;

import org.hesperides.core.presentation.config.TestAppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exemple de test unitaire de controller
 */
@RunWith(SpringRunner.class)
@WebMvcTest(org.hesperides.core.presentation.controllers.AbstractController.class)
@ContextConfiguration(classes = TestAppConfig.class)
public class ModulesControllerTest extends AbstractControllerTest {

    @Test
    @WithMockUser
    public void getModulesNamesTest() throws Exception {
        List<String> modulesList = Arrays.asList("module1", "module2", "module3");
        given(moduleUseCases.getModulesName()).willReturn(modulesList);

        this.mvc.perform(get("/modules")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().json(modulesList.toString()));
    }
}