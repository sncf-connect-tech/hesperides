package oldplatformscenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformBuilder;
import org.hesperides.test.bdd.platforms.OldPlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetInstancesModel extends HesperidesScenario implements En {

    @Autowired
    private OldPlatformClient oldPlatformClient;
    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;
    @Autowired
    private OldModuleBuilder moduleBuilder;

    public GetInstancesModel() {

        When("^I get the instance model$", () -> {
            testContext.setResponseEntity(oldPlatformClient.getInstancesModel(oldPlatformBuilder.buildInput(), moduleBuilder.getPropertiesPath()));
        });

        Then("^the instance model is successfully retrieved$", () -> {
            assertOK();
            InstancesModelOutput expectedInstancesModel = oldPlatformBuilder.buildInstancesModel();
            InstancesModelOutput actualInstancesModel = testContext.getResponseBody(InstancesModelOutput.class);
            assertEquals(expectedInstancesModel, actualInstancesModel);
        });
    }
}
