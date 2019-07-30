package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.PlatformBuilder;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetInstancesModel extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetInstancesModel() {

        When("^I get the instance model$", () -> {
            testContext.setResponseEntity(platformClient.getInstancesModel(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath()));
        });

        Then("^the instance model is successfully retrieved$", () -> {
            assertOK();
            InstancesModelOutput expectedInstancesModel = platformBuilder.buildInstancesModel();
            InstancesModelOutput actualInstancesModel = ((ResponseEntity<InstancesModelOutput>) testContext.getResponseEntity()).getBody();
            assertEquals(expectedInstancesModel, actualInstancesModel);
        });
    }
}
