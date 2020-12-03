package org.hesperides.test.bdd.technos.scenarios;

import io.cucumber.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class SearchTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;

    public SearchTechnos() {

        When("^I search for one specific techno$", () -> technoClient.searchTechnos("test-techno 0.3"));

        When("^I search for some of these technos(?:, limiting the number of results to (\\d+))?$", (Integer resultsCount) -> {
            technoClient.searchTechnos("test-techno", resultsCount == null ? 0 : resultsCount);
        });

        When("^I search for a techno that does not exist$", () -> technoClient.searchTechnos("nope"));

        When("I search for the techno named {string}", (String searchInput) -> technoClient.searchTechnos(searchInput));

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
