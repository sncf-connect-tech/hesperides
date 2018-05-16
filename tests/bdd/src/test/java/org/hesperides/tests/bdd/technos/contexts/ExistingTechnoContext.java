package org.hesperides.tests.bdd.technos.contexts;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;

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

            technoKey = new TemplateContainer.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        });
    }

    public TemplateContainer.Key getTechnoKey() {
        return technoKey;
    }
}
