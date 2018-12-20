package org.hesperides.tests.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.platforms.PlatformBuilder;
import org.hesperides.tests.bdd.platforms.PlatformClient;
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
            testContext.responseEntity = platformClient.getInstancesModel(platformBuilder.buildInput(), moduleBuilder.getPropertiesPath());
        });

        Then("^the instance model is successfully retrieved$", () -> {
            assertOK();
            InstancesModelOutput expectedInstancesModel = platformBuilder.buildInstancesModel();
            InstancesModelOutput actualInstancesModel = ((ResponseEntity<InstancesModelOutput>) testContext.responseEntity).getBody();
            assertEquals(expectedInstancesModel, actualInstancesModel);
        });
    }
}
