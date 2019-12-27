package org.hesperides.test.bdd.applications.scenarios;

import io.cucumber.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.hesperides.test.bdd.platforms.scenarios.CreatePlatforms;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchApplications extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private CreatePlatforms createPlatforms;

    public SearchApplications() {

        Given("^a list of (\\d+) applications prefixed by \"([^\"]*)\"(?: with (\\d+) platforms prefixed by \"([^\"]*)\" in each application)?$", (
                Integer applicationsCount, String applicationPrefix, Integer platformsCount, String platformPrefix) -> {

            for (int i = 0; i < applicationsCount; i++) {
                platformBuilder.withApplicationName(applicationPrefix + "-" + (i + 1));

                if (platformsCount == null) {
                    createPlatforms.createPlatform();
                } else {
                    for (int j = 0; j < platformsCount; j++) {
                        platformBuilder.withPlatformName(platformPrefix + "-" + (j + 1));
                        createPlatforms.createPlatform();
                    }
                }
            }
        });

        Given("^an application named ([^ ]+)(?: with a platform named (.+))?$", (String applicationName, String platformName) -> {
            platformBuilder.withApplicationName(applicationName);
            if (StringUtils.isNotEmpty(platformName)) {
                platformBuilder.withPlatformName(platformName);
            }
            createPlatforms.createPlatform();
        });

        When("^I( try to)? search for the application \"(.*?)\"", (String tryTo, String applicationName) -> {
            platformClient.searchApplication(applicationName, tryTo);
        });

        Then("^the application (?:list|search result) contains (\\d+) entr(?:y|ies)?$", (Integer nbEntries) -> {
            assertOK();
            List<SearchResultOutput> result = testContext.getResponseBodyAsList();
            assertEquals(nbEntries.intValue(), result.size());
        });

        Then("^the application \"(.*?)\" is found$", (String applicationName) -> {
            assertOK();
            List<SearchResultOutput> result = testContext.getResponseBodyAsList();
            assertEquals(applicationName, result.get(0).getName());
        });

        Then("^the application search is rejected with a bad request error$", this::assertBadRequest);
    }
}
