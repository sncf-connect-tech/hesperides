package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class SearchTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    public SearchTechnos() {

        Given("^a list of (\\d+) technos$", (Integer nbTechnos) -> {
            for (int i = 0; i < nbTechnos; i++) {
                technoBuilder.withName("a-techno").withVersion("0.0." + i + 1);
                technoClient.create(templateBuilder.build(), technoBuilder.build());
            }
        });

        When("^I search for one specific techno$", () -> {
            testContext.responseEntity = technoClient.search("a-techno 0.0.3");
        });

        When("^I search for some of those technos$", () -> {
            testContext.responseEntity = technoClient.search("a-techno");
        });

        When("^I search for a techno that does not exist$", () -> {
            testContext.responseEntity = technoClient.search("nope");
        });

        Then("^the techno is found$", () -> {
            assertOK();
            assertEquals(1, getBodyAsArray().length);
        });

        Then("^the list of techno results is limited to (\\d+) items$", (Integer limit) -> {
            assertOK();
            assertEquals(limit.intValue(), getBodyAsArray().length);
        });
    }
}
