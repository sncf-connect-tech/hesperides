package org.hesperides.tests.bdd.modules.contexts;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ExistingTemplateContext extends CucumberSpringBean implements En {

    private TemplateIO templateInput;

    @Autowired
    private ExistingModuleContext existingModule;

    public ExistingTemplateContext() {

        Given("^an existing template in this module$", () -> {
            addTemplateToModule("templateName");
        });

        Given("^multiple templates in this module$", () -> {
            for (int i = 0; i < 6; i++) {
                addTemplateToModule("templateName" + i);
            }
        });
    }

    public ExistingModuleContext getExistingModuleContext() {
        return existingModule;
    }

    public TemplateIO getTemplateInput() {
        return templateInput;
    }

    private void addTemplateToModule(String templateName) {
        TemplateIO.FileRightsIO rights = new TemplateIO.FileRightsIO(true, true, true);
        templateInput = new TemplateIO(null, templateName, "template.json", "/location", "content",
                new TemplateIO.RightsIO(rights, rights, rights), 0L);

        ResponseEntity<TemplateIO> response = rest.getTestRest().postForEntity(existingModule.getModuleLocation() + "/templates", templateInput, TemplateIO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    public String getTemplateLocation() {
        return String.format("/modules/%s/%s/%s/templates/%s",
                existingModule.getModuleKey().getName(),
                existingModule.getModuleKey().getVersion(),
                existingModule.getModuleKey().getVersionType(),
                templateInput.getName());
    }
}
