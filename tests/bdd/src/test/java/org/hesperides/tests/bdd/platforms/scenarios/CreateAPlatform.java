package org.hesperides.tests.bdd.platforms.scenarios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import cucumber.api.java8.En;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.hesperides.presentation.io.platforms.PlatformIO;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.PlatformAssertions;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;

public class CreateAPlatform extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    private PlatformIO platformInput;
    private ResponseEntity<PlatformIO> response;
    private ResponseEntity<String> rawResponse;

    public CreateAPlatform() {
        Given("a platform to create$", () -> {
            platformInput = PlatformSamples.buildPlatformInputWithValues(PlatformSamples.DEFAULT_PLATFORM_NAME);
        });
        Given("a platform to create, named \"([^\"]*)\"$", (String name) -> {
            platformInput = PlatformSamples.buildPlatformInputWithValues(name);
        });

        When("^creating this platform$", () -> {
            response = platformContext.createPlatform(platformInput);
        });

        When("^creating this faulty platform$", () -> {
            rawResponse = platformContext.failCreatingPlatform(platformInput);
        });

        Then("^the platform is successfully created$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PlatformIO platformOutput = response.getBody();
            PlatformIO expectedPlatformOutput = PlatformSamples.getPlatformOutputWithDefaultValues();
            PlatformAssertions.assertPlatform(expectedPlatformOutput, platformOutput);
        });
        Then("^a ([45][0-9][0-9]) error is returned, blaming \"([^\"]+)\"$", (Integer httpCode, String message) -> {
            assertEquals(HttpStatus.valueOf(httpCode), rawResponse.getStatusCode());
            assertThat(rawResponse.getBody(), Matchers.containsString(message));
        });
    }
}
