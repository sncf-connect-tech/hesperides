package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.core.presentation.io.platforms.PlatformInput;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetPlatformsUsingModule implements En {
    @Autowired
    private PlatformContext platformContext;

    @Autowired
    private ModuleContext moduleContext;

    private ResponseEntity<ModulePlatformsOutput[]> response;

    public GetPlatformsUsingModule() {
        Given("^existing platforms containing this module$", () -> {
            for (int x = 0; x < 2; x++) {
                PlatformInput p = PlatformSamples.buildPlatformInputWithExistingModule("p" + x, "test", "1.0.0");
                platformContext.createPlatform(p);
            }
        });

        When("^retrieving the platforms containing this module$", () -> {
            response = platformContext.retrieveExistingPlatformsUsingModule(moduleContext.getModuleKey());
        });

        Then("^the platforms are successfully retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<ModulePlatformsOutput> modulePlatformsOutputs = Arrays.asList(response.getBody());
            assertEquals(2, modulePlatformsOutputs.size());
        });
    }
}
