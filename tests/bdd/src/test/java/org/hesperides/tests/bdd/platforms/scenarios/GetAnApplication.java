package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.PlatformAssertions;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetAnApplication extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    private ResponseEntity<ApplicationOutput> response;

    public GetAnApplication() {

        When("^retrieving this platform's application$", () -> {
            response = platformContext.retrieveExistingApplication();
        });

        Then("^the application is successfully retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            ApplicationOutput applicationOutput = response.getBody();
            ApplicationOutput expectedApplicationOutput = PlatformSamples.getApplicationOutputWithDefaultValues();
            PlatformAssertions.assertApplication(expectedApplicationOutput, applicationOutput);
        });
    }
}
