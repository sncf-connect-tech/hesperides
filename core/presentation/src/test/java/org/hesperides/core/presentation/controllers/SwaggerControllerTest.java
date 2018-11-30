package org.hesperides.core.presentation.controllers;

import org.hesperides.core.presentation.config.TestAppConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AbstractController.class)
@ContextConfiguration(classes = TestAppConfig.class)
public class SwaggerControllerTest extends AbstractControllerTest {

    @Test
    @WithMockUser
    public void ensureSnakecaseProperties() throws Exception {
        this.mvc.perform(get("/v2/api-docs").accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definitions.ModuleIO.properties.version_id").exists())
                .andExpect(jsonPath("$.definitions.ModuleIO.properties.versionId").doesNotExist())
                .andExpect(jsonPath("$.definitions.ModuleIO.properties.working_copy").exists())
                .andExpect(jsonPath("$.definitions.ModuleIO.properties.workingCopy").doesNotExist())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.application_name").exists())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.applicationName").doesNotExist())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.modules").exists())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.platform_name").exists())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.platformName").doesNotExist())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.production").exists())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.version_id").exists())
                .andExpect(jsonPath("$.definitions.PlatformIO.properties.versionId").doesNotExist())
                .andExpect(jsonPath("$.definitions.PropertiesIO.properties.iterable_properties").exists())
                .andExpect(jsonPath("$.definitions.PropertiesIO.properties.iterableValuedProperties").doesNotExist())
                .andExpect(jsonPath("$.definitions.PropertiesIO.properties.key_value_properties").exists())
                .andExpect(jsonPath("$.definitions.PropertiesIO.properties.globalProperties").doesNotExist())
                .andExpect(jsonPath("$.definitions.TechnoIO.properties.working_copy").exists())
                .andExpect(jsonPath("$.definitions.TechnoIO.properties.workingCopy").doesNotExist())
                .andExpect(jsonPath("$.definitions.TemplateIO.properties.version_id").exists())
                .andExpect(jsonPath("$.definitions.TemplateIO.properties.versionId").doesNotExist());
    }
}