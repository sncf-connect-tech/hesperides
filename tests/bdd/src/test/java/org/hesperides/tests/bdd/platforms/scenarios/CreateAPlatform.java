package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hamcrest.Matchers;
import org.hesperides.core.presentation.io.platforms.PlatformInput;
import org.hesperides.core.presentation.io.platforms.PlatformOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.PlatformAssertions;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CreateAPlatform extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    private PlatformInput platformInput;
    private ResponseEntity<PlatformOutput> response;
    private ResponseEntity<String> rawResponse;

    public CreateAPlatform() {
        Given("a platform to create$", () -> {
            platformInput = PlatformSamples.buildPlatformInputWithName(PlatformSamples.DEFAULT_PLATFORM_NAME);
        });

        Given("a platform to create, named \"([^\"]*)\"$", (String name) -> {
            platformInput = PlatformSamples.buildPlatformInputWithName(name);
        });

        When("^creating this platform$", () -> {
            response = platformContext.createPlatform(platformInput);
        });

        When("^creating this faulty platform$", () -> {
            rawResponse = platformContext.failCreatingPlatform(platformInput);
        });

        Then("^the platform is successfully created$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            PlatformOutput platformOutput = response.getBody();
            PlatformOutput expectedPlatformOutput = PlatformSamples.getPlatformOutputWithDefaultValues();
            PlatformAssertions.assertPlatform(expectedPlatformOutput, platformOutput);
        });

        Then("^a ([45][0-9][0-9]) error is returned, blaming \"([^\"]+)\"$", (Integer httpCode, String message) -> {
            assertEquals(HttpStatus.valueOf(httpCode), rawResponse.getStatusCode());
            assertThat(rawResponse.getBody(), Matchers.containsString(message));
        });
    }
}
