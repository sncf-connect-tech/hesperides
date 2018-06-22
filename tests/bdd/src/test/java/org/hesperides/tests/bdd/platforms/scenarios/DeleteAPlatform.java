package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteAPlatform extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    public DeleteAPlatform() {
        When("^deleting this platform$", () -> {
            platformContext.deleteExistingPlatform();
        });

        Then("^the platform is successfully deleted$", () -> {
            ResponseEntity<String> response = failTryingToRetrievePlatform();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        });
    }

    private ResponseEntity<String> failTryingToRetrievePlatform() {
        String applicationName = platformContext.getPlatformKey().getApplicationName();
        String platformName = platformContext.getPlatformKey().getPlatformName();
        return rest.doWithErrorHandlerDisabled(rest -> rest.getForEntity("/applications/{application_name}/platforms/{platform_name}",
                String.class, applicationName, platformName));
    }
}
