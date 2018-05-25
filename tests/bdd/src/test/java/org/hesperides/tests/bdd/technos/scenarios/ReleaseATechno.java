package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.ExistingTechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ReleaseATechno extends CucumberSpringBean implements En {

    private ResponseEntity<TechnoIO> response;

    @Autowired
    private ExistingTechnoContext existingTechno;

    public ReleaseATechno() {
        When("^releasing this techno$", () -> {
            TemplateContainer.Key technoKey = existingTechno.getTechnoKey();
            response = rest.getTestRest().postForEntity("/templates/packages/create_release?techno_name={technoName}&techno_version={technoVersion}",
                    null, TechnoIO.class,
                    technoKey.getName(), technoKey.getVersion());
        });

        Then("^the techno is successfully released$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TechnoIO technoOutput = response.getBody();
            TemplateContainer.Key technoKey = existingTechno.getTechnoKey();
            assertEquals(technoKey.getName(), technoOutput.getName());
            assertEquals(technoKey.getVersion(), technoOutput.getVersion());
            assertEquals(false, technoOutput.isWorkingCopy());
        });
    }

    //TODO Tester avec templates
}
