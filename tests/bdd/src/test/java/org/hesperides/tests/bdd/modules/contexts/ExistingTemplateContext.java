package org.hesperides.tests.bdd.modules.contexts;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.inputs.RightsInput;
import org.hesperides.presentation.inputs.TemplateInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

public class ExistingTemplateContext extends CucumberSpringBean implements En {

    private TemplateInput templateInput;
    private URI templateLocation;

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

    public URI getTemplateLocation() {
        return templateLocation;
    }

    public ExistingModuleContext getExistingModuleContext() {
        return existingModule;
    }

    public TemplateInput getTemplateInput() {
        return templateInput;
    }

    private void addTemplateToModule(String templateName) {
        RightsInput.FileRights rights = new RightsInput.FileRights(true, true, true);
        templateInput = new TemplateInput(templateName, "template.json", "/location", "content",
                new RightsInput(rights, rights, rights), 0L);

        TemplateContainer.Key moduleKey = existingModule.getModuleKey();
        templateLocation = rest.postForLocationReturnAbsoluteURI("/modules/{moduleName}/{moduleVersion}/workingcopy/templates/", templateInput,
                moduleKey.getName(), moduleKey.getVersion());
    }
}
