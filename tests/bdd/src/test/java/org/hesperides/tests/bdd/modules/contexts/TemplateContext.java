package org.hesperides.tests.bdd.modules.contexts;

import cucumber.api.java8.En;
import cucumber.api.java8.StepdefBody;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class TemplateContext implements En {

    @Autowired
    private ModuleContext moduleContext;

    @Autowired
    private HesperidesTestRestTemplate rest;

    public TemplateContext() {
        Given("^an existing template in this module$", (StepdefBody.A0) this::addTemplateToExistingModule);
    }

    public ResponseEntity<TemplateIO> retrieveExistingTemplate() {
        return retrieveExistingTemplate(TemplateBuilder.DEFAULT_NAME);
    }

    public ResponseEntity<TemplateIO> retrieveExistingTemplate(String name) {
        return rest.getTestRest().getForEntity(getTemplateURI(name), TemplateIO.class);
    }

    public ResponseEntity<TemplateIO> addTemplateToExistingModule() {
        TemplateIO templateInput = new TemplateBuilder().build();
        return addTemplateToExistingModule(templateInput);
    }

    public ResponseEntity<TemplateIO> addTemplateToExistingModule(String templateName) {
        TemplateIO templateInput = new TemplateBuilder().withName(templateName).build();
        return addTemplateToExistingModule(templateInput);
    }

    public ResponseEntity<TemplateIO> addTemplateToExistingModule(TemplateIO templateInput) {
        ResponseEntity<TemplateIO> response = rest.getTestRest().postForEntity(getTemplatesURI(), templateInput, TemplateIO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response;
    }

    public ResponseEntity updateTemplate(TemplateIO templateInput) {
        return rest.putForEntity(getTemplatesURI(), templateInput, TemplateIO.class);
    }

    public void deleteTemplate() {
        deleteTemplate(TemplateBuilder.DEFAULT_NAME);
    }

    public void deleteTemplate(String templateName) {
        rest.getTestRest().delete(getTemplateURI(templateName));
    }

    // URIs

    public String getTemplatesURI() {
        return moduleContext.getModuleURI() + "/templates";
    }

    public String getTemplateURI(String templateName) {
        return getTemplatesURI() + "/" + templateName;
    }
}
