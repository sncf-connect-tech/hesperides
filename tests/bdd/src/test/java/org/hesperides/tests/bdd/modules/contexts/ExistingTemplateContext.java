package org.hesperides.tests.bdd.modules.contexts;

import cucumber.api.java8.En;
import org.hesperides.presentation.inputs.RightsInput;
import org.hesperides.presentation.inputs.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

public class ExistingTemplateContext extends CucumberSpringBean implements En {

    private TemplateInput templateInput;
    private URI templateLocation;

    @Autowired
    private ExistingModuleContext existingModuleContext;

    public ExistingTemplateContext() {
        Given("^an existing template in this module$", () -> {
            RightsInput.FileRights rights = new RightsInput.FileRights(true, true, true);
            templateInput = new TemplateInput("templateName", "template.name", "template.location", "content",
                    new RightsInput(rights, rights, rights), 0L);
            templateLocation = rest.postForLocationReturnAbsoluteURI("/modules/{id}/{version}/workingcopy/templates/", templateInput,
                    existingModuleContext.getModuleKey().getName(), existingModuleContext.getModuleKey().getVersion());
        });
    }

    public URI getTemplateLocation() {
        return templateLocation;
    }

    public ExistingModuleContext getExistingModuleContext() {
        return existingModuleContext;
    }
}
