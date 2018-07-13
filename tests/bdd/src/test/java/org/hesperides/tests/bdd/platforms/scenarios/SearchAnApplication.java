package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchAnApplication extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    private ResponseEntity<SearchResultOutput[]> response;

    public SearchAnApplication() {

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

                    platformContext.createPlatform(PlatformSamples.buildPlatformInputWithValues("REL" + j, applications[i]));
                }
            }
        });

        When("^searching for one of those applications$", () -> {
            response = platformContext.searchApplication("AAA");
        });

        Then("^application found$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SearchResultOutput> applications = Arrays.asList(response.getBody());
            assertEquals(1, applications.size());
            assertEquals("AAA", applications.get(0).getName());
        });

        When("^searching for some of those applications$", () -> {
            response = platformContext.searchApplication("AA");
        });

        Then("^the number of application results is (\\d+)$", (Integer numberOfResults) -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SearchResultOutput> applications = Arrays.asList(response.getBody());
            assertEquals(numberOfResults.intValue(), applications.size());
        });

        When("^searching for an application that does not exist$", () -> {
            response = platformContext.searchApplication("ZZZ");
        });
    }
}
