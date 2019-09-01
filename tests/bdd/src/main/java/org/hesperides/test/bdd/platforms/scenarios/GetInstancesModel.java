package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.InstanceBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetInstancesModel extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private InstanceBuilder instanceBuilder;
    @Autowired
    private SaveProperties saveProperties;

    public GetInstancesModel() {

        Given("^the platform has an instance property with the same name as a global property$", () -> {
            deployedModuleBuilder.withValuedProperty("module-property", "{{global-property}}");
            saveProperties.saveValuedProperties();

            instanceBuilder.withValuedProperty("global-property", "instance-property-value");
            saveProperties.saveInstanceProperties();

            platformBuilder.withGlobalProperty("global-property", "global-value");
            saveProperties.saveGlobalProperties();
        });

        Given("^the platform has instance properties with the same name as another module property$", () -> {
            deployedModuleBuilder.withValuedProperty("module-property-a", "{{module-property-b}}");
            deployedModuleBuilder.withValuedProperty("module-property-b", "module-property-value");
            saveProperties.saveValuedProperties();

            instanceBuilder.withValuedProperty("module-property-b", "instance-property-value");
            saveProperties.saveInstanceProperties();
        });

        Given("^the platform has instance properties with the same name as the module property that it's declared in$", () -> {
            deployedModuleBuilder.withValuedProperty("module-property", "{{module-property}}");
            saveProperties.saveValuedProperties();

            instanceBuilder.withValuedProperty("module-property", "instance-property-value");
            saveProperties.saveInstanceProperties();
        });

        Given("^the platform has multiple instance properties declared in the same property value$", () -> {
            deployedModuleBuilder.withValuedProperty("module-property", "{{instance-property-a}}{{instance-property-b}}");
            saveProperties.saveValuedProperties();

            instanceBuilder.withValuedProperty("instance-property-a", "instance-value-a");
            instanceBuilder.withValuedProperty("instance-property-b", "instance-value-b");
            saveProperties.saveInstanceProperties();
        });

        Given("^the platform has an instance property declared in two different module properties$", () -> {
            deployedModuleBuilder.withValuedProperty("module-property-a", "{{instance-property}}");
            deployedModuleBuilder.withValuedProperty("module-property-b", "{{instance-property}}");
            saveProperties.saveValuedProperties();

            instanceBuilder.withValuedProperty("instance-property", "instance-value");
            saveProperties.saveInstanceProperties();
        });

        When("^I get the instance model$", () -> {
            platformClient.getInstancesModel(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
        });

        Then("^the instance model is successfully retrieved$", () -> {
            assertOK();
            InstancesModelOutput expectedInstancesModel = platformBuilder.buildInstanceModel();
            InstancesModelOutput actualInstancesModel = testContext.getResponseBody(InstancesModelOutput.class);
            assertEquals(expectedInstancesModel, actualInstancesModel);
        });
    }
}
