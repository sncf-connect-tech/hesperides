package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class ReleaseATechno extends CucumberSpringBean implements En {

    private ResponseEntity<TechnoIO> response;

    @Autowired
    private TechnoContext technoContext;

    public ReleaseATechno() {
        When("^releasing this techno$", () -> {
            response = technoContext.releaseTechno();
        });

        Then("^the techno is successfully released$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TechnoIO technoOutput = response.getBody();
            TemplateContainer.Key technoKey = technoContext.getTechnoKey();
            assertEquals(technoKey.getName(), technoOutput.getName());
            assertEquals(technoKey.getVersion(), technoOutput.getVersion());
            assertEquals(false, technoOutput.isWorkingCopy());
        });
    }

    //TODO Tester avec templates
}
