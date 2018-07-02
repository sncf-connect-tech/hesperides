package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.platforms.PlatformInput;
import org.hesperides.presentation.io.platforms.SearchPlatformOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SearchAPlatform extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;
    private ResponseEntity<SearchPlatformOutput[]> response;

    public SearchAPlatform() {
        Given("^a list of 25 platforms$", () -> {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    PlatformInput platformInput = PlatformSamples.buildPlatformInputWithValues2("APP-" + i, "TEST-" + j);
                    platformContext.createPlatform(platformInput);
                }
            }
        });

        When("^searching for one of them giving an application name and a platform name$", () -> {
            String url = "/applications/platforms/perform_search?application_name=APP-1&platform_name=TEST-2";
            response = rest.getTestRest().postForEntity(
                    url,
                    null,
                    SearchPlatformOutput[].class);
        });

        Then("^the platform is found$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SearchPlatformOutput> platformOutputList = Arrays.asList(response.getBody());
            assertEquals(1, platformOutputList.size());
            assertEquals("TEST-2", platformOutputList.get(0).getPlatformName());
        });

        When("^asking for the platform list of an application$", () -> {
            String url = "/applications/platforms/perform_search?application_name=APP-1";
            response = rest.getTestRest().postForEntity(
                    url,
                    null,
                    SearchPlatformOutput[].class);
        });

        Then("^platform list is established for the targeted application$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<SearchPlatformOutput> platformOutputList = Arrays.asList(response.getBody());
            assertEquals(5, platformOutputList.size());
        });
    }
}
