package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.platforms.PlatformIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.PlatformAssertions;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetAPlatform extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    private ResponseEntity<PlatformIO> response;

    public GetAPlatform() {

        When("^retrieving this platform$", () -> {
            response = platformContext.retrieveExistingPlatform();
        });

        Then("^the platform is successfully retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PlatformIO platformOutput = response.getBody();
            PlatformIO expectedPlatformOutput = PlatformSamples.getPlatformOutputWithDefaultValues();
            PlatformAssertions.assertPlatform(expectedPlatformOutput, platformOutput);
        });
    }
}
