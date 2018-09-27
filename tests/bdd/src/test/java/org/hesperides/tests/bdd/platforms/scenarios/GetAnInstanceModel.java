package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.InstanceModelOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.contexts.PlatformContext;
import org.hesperides.tests.bdd.platforms.samples.InstanceSamples;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class GetAnInstanceModel extends CucumberSpringBean implements En {

    @Autowired
    private PlatformContext platformContext;

    private ResponseEntity<InstanceModelOutput> response;

    public GetAnInstanceModel() {

        When("^retrieving the instanceModel contained in that platform( whitout instance)?$", (String ) -> {
            response = platformContext.retriveInstanceModelByPlatformAndModulPath(PlatformSamples.MODULE1_OUTPUT_PROPERTIES_PATH);
        });

        Then("^the instanceModel is successfully retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());

            InstanceModelOutput instanceModelOutput = response.getBody();
            InstanceModelOutput.InstancePropertyOutput expectedInstancePropertyOutput1 = getExpectedInstancePropertyOutput(InstanceSamples.DEFAULT_PROPERTY_NAME1);
            InstanceModelOutput.InstancePropertyOutput expectedInstancePropertyOutput2 = getExpectedInstancePropertyOutput(InstanceSamples.DEFAULT_PROPERTY_NAME2);
            InstanceModelOutput expectedInstanceModelOutput = new InstanceModelOutput(Arrays.asList(expectedInstancePropertyOutput1, expectedInstancePropertyOutput2));
            assertEquals(expectedInstanceModelOutput, instanceModelOutput);
        });
    }

    @NotNull
    private InstanceModelOutput.InstancePropertyOutput getExpectedInstancePropertyOutput(String name) {
        return new InstanceModelOutput.InstancePropertyOutput(name,  "", false, null, null, false);
    }

}
