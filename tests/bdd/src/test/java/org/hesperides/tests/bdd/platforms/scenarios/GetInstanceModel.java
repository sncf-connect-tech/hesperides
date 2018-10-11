package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.InstanceModelOutput;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

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

        When("^I get the instance model$", () -> {
            responseEntity = platformClient.getInstanceModel(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath());
        });

        Then("^the instance model is successfully retrieved$", () -> {
            assertOK(responseEntity);
            InstanceModelOutput expectedInstanceModel = platformBuilder.buildInstanceModel();
            InstanceModelOutput actualInstanceModel = responseEntity.getBody();
            assertEquals(expectedInstanceModel, actualInstanceModel);
        });
    }
}
