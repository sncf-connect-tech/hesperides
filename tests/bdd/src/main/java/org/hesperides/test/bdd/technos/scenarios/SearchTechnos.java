package org.hesperides.test.bdd.technos.scenarios;

import io.cucumber.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
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

        When("^I search for one specific techno$", () -> technoClient.searchTechnos("test-techno 0.3"));

        When("^I search for some of those technos(?:, limiting the number of results to (\\d+))?$", (Integer resultsCount) -> {
            technoClient.searchTechnos("test-techno", resultsCount == null ? 0 : resultsCount);
        });

        When("^I search for a techno that does not exist$", () -> technoClient.searchTechnos("nope"));

        Then("^the techno is found$", () -> {
            assertOK();
            assertEquals(1, testContext.getResponseBodyArrayLength());
        });

        Then("^the list of techno results is limited to (\\d+) items$", (Integer limit) -> {
            assertOK();
            assertEquals(limit.intValue(), testContext.getResponseBodyArrayLength());
        });
    }
}
