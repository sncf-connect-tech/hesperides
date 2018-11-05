package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;
import static org.junit.Assert.assertEquals;

public class SearchApplications extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    public SearchApplications() {

        Given("^a list of (\\d+) applications prefixed by \"([^\"]*)\"( with (\\d+) platforms prefixed by \"([^\"]*)\" in each application)?$", (
                Integer nbApplications, String applicationPrefix, String withPlatform, Integer nbPlatforms, String platformPrefix) -> {

            for (int i = 0; i < nbApplications; i++) {
                platformBuilder.withApplicationName(applicationPrefix + "-" + (i + 1));

                if (StringUtils.isNotEmpty(withPlatform)) {
                    for (int j = 0; j < nbPlatforms; j++) {
                        platformBuilder.withPlatformName(platformPrefix + "-" + (j + 1));
                        platformClient.create(platformBuilder.buildInput());
                    }
                } else {
                    platformClient.create(platformBuilder.buildInput());
                }
            }
        });

        When("^I( try to)? search for the application \"(.*?)\"", (String tryTo, String applicationName) -> {
            testContext.responseEntity = platformClient.searchApplication(applicationName, getResponseType(tryTo, SearchResultOutput[].class));
        });

        Then("^the application search result contains (\\d+) entr(?:y|ies)?$", (Integer nbEntries) -> {
            assertOK();
            List<SearchResultOutput> result = Arrays.asList((SearchResultOutput[]) testContext.responseEntity.getBody());
            assertEquals(nbEntries.intValue(), result.size());
        });

        Then("^the application \"(.*?)\" is found$", (String applicationName) -> {
            assertOK();
            List<SearchResultOutput> result = Arrays.asList((SearchResultOutput[]) testContext.responseEntity.getBody());
            assertEquals(applicationName, result.get(0).getName());
        });

        Then("^the application search is rejected with a bad request error$", () -> {
            assertBadRequest();
        });
    }
}
