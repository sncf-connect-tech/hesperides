package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.inputs.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingTemplateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetATemplate extends CucumberSpringBean implements En {

    private ResponseEntity<TemplateView> response;

    @Autowired
    private ExistingTemplateContext existingTemplateContext;

    public GetATemplate() {

        When("^retrieving this template$", () -> {
            TemplateContainer.Key moduleKey = existingTemplateContext.getExistingModuleContext().getModuleKey();
            String templateName = existingTemplateContext.getTemplateInput().getName();
            response = rest.getTestRest().getForEntity(String.format("/modules/%s/%s/%s/templates/%s",
                    moduleKey.getName(), moduleKey.getVersion(), moduleKey.getVersionType(), templateName), TemplateView.class);
        });

        Then("^the template is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TemplateView template = response.getBody();
            TemplateInput templateInput = existingTemplateContext.getTemplateInput();
            assertEquals(templateInput.getName(), template.getName());
            assertEquals(templateInput.getFilename(), template.getFilename());
            assertEquals(templateInput.getLocation(), template.getLocation());
            assertEquals(templateInput.getContent(), template.getContent());
            assertEquals(templateInput.getRights().getUser().getRead(), template.getRights().getUser().getRead());
            assertEquals(templateInput.getRights().getUser().getWrite(), template.getRights().getUser().getWrite());
            assertEquals(templateInput.getRights().getUser().getExecute(), template.getRights().getUser().getExecute());
            assertEquals(templateInput.getRights().getGroup().getRead(), template.getRights().getGroup().getRead());
            assertEquals(templateInput.getRights().getGroup().getWrite(), template.getRights().getGroup().getWrite());
            assertEquals(templateInput.getRights().getGroup().getExecute(), template.getRights().getGroup().getExecute());
            assertEquals(templateInput.getRights().getOther().getRead(), template.getRights().getOther().getRead());
            assertEquals(templateInput.getRights().getOther().getWrite(), template.getRights().getOther().getWrite());
            assertEquals(templateInput.getRights().getOther().getExecute(), template.getRights().getOther().getExecute());
            assertEquals(1, template.getVersionId().longValue());
        });
    }
}
