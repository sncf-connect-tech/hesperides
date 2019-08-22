package org.hesperides.test.bdd.applications.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchApplications extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;

    public SearchApplications() {

        Given("^a list of (\\d+) applications prefixed by \"([^\"]*)\"( with (\\d+) platforms prefixed by \"([^\"]*)\" in each application)?$", (
                Integer nbApplications, String applicationPrefix, String withPlatform, Integer nbPlatforms, String platformPrefix) -> {

            for (int i = 0; i < nbApplications; i++) {
                oldPlatformBuilder.withApplicationName(applicationPrefix + "-" + (i + 1));

                if (StringUtils.isNotEmpty(withPlatform)) {
                    for (int j = 0; j < nbPlatforms; j++) {
                        oldPlatformBuilder.withPlatformName(platformPrefix + "-" + (j + 1));
                        oldPlatformClient.create(oldPlatformBuilder.buildInput());
                    }
                } else {
                    oldPlatformClient.create(oldPlatformBuilder.buildInput());
                }
            }
        });

        Given("^an application named ([^ ]+)(?: with a platform named (.+))?$", (String applicationName, String platformName) -> {
            oldPlatformBuilder.withApplicationName(applicationName);
            if (StringUtils.isNotEmpty(platformName)) {
                oldPlatformBuilder.withPlatformName(platformName);
            }
            oldPlatformClient.create(oldPlatformBuilder.buildInput());
        });

        When("^I( try to)? search for the application \"(.*?)\"", (String tryTo, String applicationName) -> {
            testContext.setResponseEntity(oldPlatformClient.searchApplication(applicationName, getResponseType(tryTo, SearchResultOutput[].class)));
        });

        Then("^the application (?:list|search result) contains (\\d+) entr(?:y|ies)?$", (Integer nbEntries) -> {
            assertOK();
            List<SearchResultOutput> result = Arrays.asList(testContext.getResponseBody(SearchResultOutput[].class));
            assertEquals(nbEntries.intValue(), result.size());
        });

        Then("^the application \"(.*?)\" is found$", (String applicationName) -> {
            assertOK();
            List<SearchResultOutput> result = Arrays.asList(testContext.getResponseBody(SearchResultOutput[].class));
            assertEquals(applicationName, result.get(0).getName());
        });

        Then("^the application search is rejected with a bad request error$", this::assertBadRequest);
    }
}
