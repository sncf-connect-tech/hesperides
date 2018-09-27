package org.hesperides.tests.bddrefacto.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.tests.bddrefacto.platforms.PlatformBuilder;
import org.hesperides.tests.bddrefacto.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SearchPlatforms implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    private ResponseEntity<SearchResultOutput[]> responseEntity;

    public SearchPlatforms() {
        Given("^a list of 25 platforms$", () -> {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    platformBuilder.withApplicationName("APP-" + i).withPlatformName("TEST-" + j);
                    platformClient.create(platformBuilder.buildInput());
                }
            }
        });

        When("^searching for one of them giving an application name and a platform name$", () -> {
            String applicationName = "APP-1";
            String platformName = "TEST-2";
            responseEntity = platformClient.search(applicationName, platformName);
        });

        Then("^the platform is found$", () -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            List<SearchResultOutput> platformOutputList = Arrays.asList(responseEntity.getBody());
            assertEquals(1, platformOutputList.size());
            assertEquals("TEST-2", platformOutputList.get(0).getName());
        });

        When("^asking for the platform list of an application$", () -> {
            responseEntity = platformClient.search("APP-1");
        });

        Then("^platform list is established for the targeted application$", () -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            List<SearchResultOutput> platformOutputList = Arrays.asList(responseEntity.getBody());
            assertEquals(5, platformOutputList.size());
        });
    }

}
