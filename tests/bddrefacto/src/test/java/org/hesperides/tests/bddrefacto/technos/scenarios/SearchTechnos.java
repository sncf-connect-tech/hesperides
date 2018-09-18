package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class SearchTechnos implements En {

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity<TechnoIO[]> responseEntity;

    public SearchTechnos() {

        Given("^a list of (\\d+) technos$", (final Integer nbTechnos) -> {
            technoBuilder = new TechnoBuilder();
            templateBuilder = new TemplateBuilder();

            TemplateIO templateInput = templateBuilder.build();
            for (int i = 0; i < nbTechnos; i++) {
                TechnoIO technoInput = technoBuilder
                        .withName("a-techno")
                        .withVersion("0.0." + i + 1)
                        .build();
                technoClient.create(templateInput, technoInput, TemplateIO.class);
            }
        });

        When("^I search for one specific techno$", () -> {
            responseEntity = technoClient.search("a-techno 0.0.3");
        });

        When("^I search for some of those technos$", () -> {
            responseEntity = technoClient.search("a-techno");
        });

        When("^I search for a techno that does not exist$", () -> {
            responseEntity = technoClient.search("nope");
        });

        Then("^the techno is found$", () -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(1, responseEntity.getBody().length);
        });

        Then("^the list of techno results is limited to (\\d+) items$", (final Integer limit) -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(limit.intValue(), responseEntity.getBody().length);
        });

        Then("^the list of techno results is empty$", () -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(0, responseEntity.getBody().length);
        });
    }
}
