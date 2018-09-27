package org.hesperides.tests.bddrefacto.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.tests.bddrefacto.platforms.PlatformBuilder;
import org.hesperides.tests.bddrefacto.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class SearchApplications implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    private ResponseEntity<SearchResultOutput[]> responseEntity;

    public SearchApplications() {

        Given("^a list of applications$", () -> {
            String applicationName = "";

            String[] applications = {"AAA", "AAB", "BBB", "CCC", "DDD", "EEE"};

            for (int i = 0; i < applications.length; i++) {
                for (int j = 0; j < 5; j++) {
                    if (i < 5) {
                        applicationName = "AAA";
                    } else if (i >= 5 && i < 10) {
                        applicationName = "BBB";
                    } else if (i >= 10 && i < 15) {
                        applicationName = "CCC";
                    } else {
                        applicationName = "DDD";
                    }
                    platformBuilder.withApplicationName(applications[i]).withPlatformName("REL" + j);
                    platformClient.create(platformBuilder.buildInput());
                }
            }
        });

        When("^searching for one of those applications$", () -> {
            responseEntity = platformClient.searchApplication("AAA");
        });

        Then("^application found$", () -> {
            assertOK(responseEntity);
            List<SearchResultOutput> applications = Arrays.asList(responseEntity.getBody());
            assertEquals(1, applications.size());
            assertEquals("AAA", applications.get(0).getName());
        });

        When("^searching for some of those applications$", () -> {
            responseEntity = platformClient.searchApplication("AA");
        });

        Then("^the number of application results is (\\d+)$", (Integer numberOfResults) -> {
            assertOK(responseEntity);
            List<SearchResultOutput> applications = Arrays.asList(responseEntity.getBody());
            assertEquals(numberOfResults.intValue(), applications.size());
        });

        When("^searching for an application that does not exist$", () -> {
            responseEntity = platformClient.searchApplication("ZZZ");
        });
    }
}
