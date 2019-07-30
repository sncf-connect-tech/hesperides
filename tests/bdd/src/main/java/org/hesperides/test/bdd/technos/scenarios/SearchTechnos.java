package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
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

        When("^I search for one specific techno$", () -> {
            testContext.setResponseEntity(technoClient.search("a-techno 0.0.3"));
        });

        When("^I search for some of those technos(?:, limiting the number of results to (\\d+))?$", (String nbResults) -> {
            Integer size = StringUtils.isEmpty(nbResults) ? 0 : Integer.valueOf(nbResults);
            testContext.setResponseEntity(technoClient.search("a-techno", size));
        });

        When("^I search for a techno that does not exist$", () -> {
            testContext.setResponseEntity(technoClient.search("nope"));
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
