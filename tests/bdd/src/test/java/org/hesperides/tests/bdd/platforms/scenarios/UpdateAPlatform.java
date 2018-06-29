package org.hesperides.tests.bdd.platforms.scenarios;

import java.util.List;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.platforms.PlatformInput;
import org.hesperides.presentation.io.platforms.PlatformOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.PlatformAssertions;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class UpdateAPlatform extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    private ResponseEntity<PlatformOutput> response;

    public UpdateAPlatform() {

        When("^updating this platform(, requiring properties copy)?$", (String withCopy) -> {
            PlatformInput platformInput = PlatformSamples.getPlatformInputWithVersionId(1L);
            response = platformContext.updatePlatform(platformInput, withCopy != null);
        });

        Then("^the platform is successfully updated(?:, but system warns about \"([^\"]+)\")?", (String warning) -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            if (warning != null) {
                final List<String> warnings = response.getHeaders().get("x-hesperides-warning");
                assertTrue("expected at least 1 custom warning", warnings != null && warnings.size() > 0);
                assertThat(warnings, hasItem(containsString(warning)));
            }
            PlatformOutput platformOutput = response.getBody();
            PlatformOutput expectedPlatformOutput = PlatformSamples.getPlatformOutputWithVersionId(2L);
            PlatformAssertions.assertPlatform(expectedPlatformOutput, platformOutput);
        });
    }
}
