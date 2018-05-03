package org.hesperides.tests.bdd.technos.contexts;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ExistingTechnoContext extends CucumberSpringBean implements En {

    TemplateContainer.Key technoKey;

    public ExistingTechnoContext() {
        Given("^an existing techno$", () -> {

            String technoName = "technoName";
            String technoVersion = "technoVersion";

            TemplateIO templateInput = new TemplateIO(
                    null,
                    "fichierTest",
                    "test.json",
                    "/home/test",
                    "{test:test}",
                    new TemplateIO.RightsIO(
                            new TemplateIO.FileRightsIO(null, null, null),
                            new TemplateIO.FileRightsIO(null, null, null),
                            new TemplateIO.FileRightsIO(null, null, null)
                    ), -1L);

            rest.getTestRest().postForEntity(
                    "/templates/packages/{technoName}/{technoVersion}/workingcopy/templates",
                    templateInput,
                    TemplateIO.class,
                    technoName,
                    technoVersion);

            technoKey = new TemplateContainer.Key(technoName, technoVersion, TemplateContainer.Type.workingcopy);
        });
    }

    public TemplateContainer.Key getTechnoKey() {
        return technoKey;
    }
}
