package org.hesperides.tests.bdd.modules.contexts;

import cucumber.api.java.en.Given;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.presentation.controllers.TemplateInput;
import org.hesperides.tests.bdd.commons.tools.HesperideTestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

public class ExistingTemplateContext {

    private TemplateInput templateInput;
    private URI templateLocation;

    @Autowired
    protected HesperideTestRestTemplate template;

    @Autowired
    private ExistingModuleContext moduleContext;

    @Given("^an existing template in this module$")
    public void anExistingTemplateInThisModule() throws Throwable {
        Template.FileRights rights = new Template.FileRights(true, true, true);
        templateInput = new TemplateInput("templateName", "template.name", "template.location", "content",
                new Template.Rights(rights, rights, rights));
        templateLocation = template.postForLocationReturnAbsoluteURI("/modules/{id}/{version}/workingcopy/templates/", templateInput,
                moduleContext.getModuleKey().getName(), moduleContext.getModuleKey().getVersion());
    }

    public URI getTemplateLocation() {
        return templateLocation;
    }
}
