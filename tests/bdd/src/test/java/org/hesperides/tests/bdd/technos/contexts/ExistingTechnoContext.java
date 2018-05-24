package org.hesperides.tests.bdd.technos.contexts;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;

public class ExistingTechnoContext extends CucumberSpringBean implements En {

    TemplateContainer.Key technoKey;

    public ExistingTechnoContext() {
        Given("^an existing techno$", () -> {
            createTechno("technoName", "technoVersion");
        });
        Given("^a list of existing technos$", () -> {
            for (int i = 0; i < 12; i++) {
                createTechno("technoName-" + i, "technoVersion-" + i);
            }
        });
    }

    private void createTechno(String name, String version) {
        //TODO Sortir dans ExistingTemplateContext
        TemplateIO templateInput = new TemplateIO(
                "fichierTest",
                null,
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
                name,
                version);

        technoKey = new TemplateContainer.Key(name, version, TemplateContainer.VersionType.workingcopy);
    }

    public TemplateContainer.Key getTechnoKey() {
        return technoKey;
    }

    public String getTechnoLocation() {
        return String.format("/templates/packages/%s/%s/%s", technoKey.getName(), technoKey.getVersion(), technoKey.getVersionType());
    }

}
