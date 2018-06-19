package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.TechnosSamples;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchATechno extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    private ResponseEntity<TechnoIO[]> response;

    public SearchATechno() {

        Given("^a list of 12 technos$", () -> {
            TemplateIO templateInput = TemplateSamples.getTemplateInputWithDefaultValues();
            for (int i = 0; i < 12; i++) {
                TechnoIO technoInput = TechnosSamples.getTechnoWithNameAndVersion("test-" + i, "1.0." + i);
                technoContext.createTechno(technoInput, templateInput);
            }
        });

        When("^searching for a specific techno$", () -> {
            response = rest.getTestRest().postForEntity("/templates/packages/perform_search?terms=test-6 1.0.6", null, TechnoIO[].class);
        });

        Then("^the techno is found$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<TechnoIO> technos = Arrays.asList(response.getBody());
            assertEquals(1, technos.size());
            TechnoIO techno = technos.get(0);
            assertEquals("test-6", techno.getName());
            assertEquals("1.0.6", techno.getVersion());
            assertEquals(true, techno.isWorkingCopy());
        });

        When("^searching for some of those technos$", () -> {
            response = rest.getTestRest().postForEntity("/templates/packages/perform_search?terms=test", null, TechnoIO[].class);
        });

        Then("^the number of techno results is (\\d+)$", (Integer numberOfResults) -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<TechnoIO> modules = Arrays.asList(response.getBody());
            assertEquals(numberOfResults.intValue(), modules.size());
        });

        When("^searching for a techno that does not exist$", () -> {
            response = rest.getTestRest().postForEntity("/templates/packages/perform_search?terms=foo", null, TechnoIO[].class);
        });
    }
}
