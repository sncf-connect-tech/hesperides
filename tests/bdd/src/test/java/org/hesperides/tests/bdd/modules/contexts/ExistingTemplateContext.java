package org.hesperides.tests.bdd.modules.contexts;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.PartialTemplateIO;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.templatecontainer.tools.TemplateSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExistingTemplateContext extends CucumberSpringBean implements En {

    @Autowired
    private ExistingModuleContext existingModuleContext;

    private List<TemplateIO> templateInputs = new ArrayList<>();

    public ExistingTemplateContext() {

        Given("^an existing template in this module$", () -> {
            addTemplateToExistingModule();
        });

        Given("^multiple templates in this module$", () -> {
            for (int i = 0; i < 6; i++) {
                String templateName = TemplateSample.TEMPLATE_NAME + i;
                addTemplateToExistingModule(templateName);
                ResponseEntity<TemplateIO> responseEntity = restGetTemplate(templateName);
                templateInputs.add(responseEntity.getBody());
            }
        });
    }

    public List<TemplateIO> getTemplateInputs() {
        return templateInputs;
    }

    public ResponseEntity<TemplateIO> getExistingTemplate() {
        return getExistingTemplate(TemplateSample.TEMPLATE_NAME);
    }

    private ResponseEntity<TemplateIO> getExistingTemplate(String name) {
        return restGetTemplate(name);
    }

    public ResponseEntity<String> failTryingToGetTemplate() {
        return rest.doWithErrorHandlerDisabled(rest -> rest.getForEntity(getTemplateURI(TemplateSample.TEMPLATE_NAME), String.class));
    }

    public ResponseEntity<PartialTemplateIO[]> getModuleTemplates() {
        return restGetListOfTemplates();
    }

    public void addTemplateToExistingModule() {
        addTemplateToExistingModule(TemplateSample.TEMPLATE_NAME);
    }

    private void addTemplateToExistingModule(String templateName) {
        TemplateIO templateInput = TemplateSample.getTemplateInput(templateName);
        ResponseEntity<TemplateIO> response = restPostTemplate(templateInput);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    public ResponseEntity failTryingToAddTemplateToExistingModule() {
        TemplateIO templateInput = TemplateSample.getTemplateInput();
        return rest.doWithErrorHandlerDisabled(rest -> rest.postForEntity(getTemplatesURI(), templateInput, String.class));
    }

    public void updateModuleTemplate(TemplateIO templateInput) {
        restPutTemplate(templateInput);
    }

    public ResponseEntity failTryingToUpdateModuleTemplate(TemplateIO templateInput) {
        return rest.doWithErrorHandlerDisabled(rest -> rest.exchange(getTemplatesURI(), HttpMethod.PUT, new HttpEntity<>(templateInput), String.class));
    }

    public void deleteExistingTemplate() {
        restDeleteTemplate(TemplateSample.TEMPLATE_NAME);
    }

    // URIs

    private String getTemplatesURI() {
        return existingModuleContext.getModuleLocation() + "/templates";
    }

    private String getTemplateURI(String templateName) {
        return String.format(getTemplatesURI() + "/" + templateName);
    }

    // REST calls

    private ResponseEntity<TemplateIO> restGetTemplate(String name) {
        return rest.getTestRest().getForEntity(getTemplateURI(name), TemplateIO.class);
    }

    private ResponseEntity<PartialTemplateIO[]> restGetListOfTemplates() {
        return rest.getTestRest().getForEntity(getTemplatesURI(), PartialTemplateIO[].class);
    }

    private ResponseEntity<TemplateIO> restPostTemplate(TemplateIO templateInput) {
        return rest.getTestRest().postForEntity(getTemplatesURI(), templateInput, TemplateIO.class);
    }

    private void restPutTemplate(TemplateIO templateInput) {
        rest.getTestRest().put(getTemplatesURI(), templateInput);
    }

    public void restDeleteTemplate(String name) {
        rest.getTestRest().delete(getTemplateURI(name));
    }
}
