package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.InstanceModelOutput;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.hesperides.tests.bdd.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class GetInstanceModel implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity<InstanceModelOutput> responseEntity;

    public GetInstanceModel() {

        When("^retrieving the instanceModel contained in that platform( whitout instance)?$", (String) -> {
            responseEntity = platformClient.getInstanceModel(platformBuilder.build(), moduleBuilder.getPropertiesPath());
        });

        Then("^the instanceModel is successfully retrieved$", () -> {
            assertOK(responseEntity);
            InstanceModelOutput actualInstanceModel = responseEntity.getBody();
            InstanceModelOutput.InstancePropertyOutput expectedInstanceProperty1 = getExpectedInstancePropertyOutput("module-foo");
            InstanceModelOutput.InstancePropertyOutput expectedInstanceProperty2 = getExpectedInstancePropertyOutput("module-bar");
            InstanceModelOutput expectedInstanceModel = new InstanceModelOutput(Arrays.asList(expectedInstanceProperty1, expectedInstanceProperty2));
            assertEquals(expectedInstanceModel, actualInstanceModel);
        });
    }

    @NotNull
    private InstanceModelOutput.InstancePropertyOutput getExpectedInstancePropertyOutput(String name) {
        return new InstanceModelOutput.InstancePropertyOutput(name, "", false, null, null, false);
    }

}
