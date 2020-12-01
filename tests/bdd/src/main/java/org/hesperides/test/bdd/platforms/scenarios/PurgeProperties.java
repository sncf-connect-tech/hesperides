package org.hesperides.test.bdd.platforms.scenarios;

import io.cucumber.java.en.Then;
import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PurgeProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    public PurgeProperties() {
        When("^I try to purge unneeded global properties of this platform$", () -> {
            PlatformIO selector = platformBuilder.buildInput();

            platformClient.cleanUnusedProperties(selector, "#", "should fail");
        });

        When("^I purge unneeded properties (?:of this platform|of module \"([^\"]+)\")$", (String moduleName) -> {
            PlatformIO selector = platformBuilder.buildInput();
            String path = moduleName == null ? null
                    : platformBuilder.findDeployedModuleBuilder(moduleName).buildPropertiesPath();

            platformClient.cleanUnusedProperties(selector, path, null);
        });

        When("^I try to purge unneeded properties of unknown module \"([^\"]+)\"$", (String badModuleName) -> {
            PlatformIO selector = platformBuilder.buildInput();
            String path = "#ABC#DEF#" + badModuleName + "#1.0#RELEASE";

            platformClient.cleanUnusedProperties(selector, path, "should fail");
        });
    }

    @Then("^the module \"([^\"]+)\" (?:contains only|still contains all) the following properties$")
    public void theModuleContainsTheseProperties(String moduleName, List<String> expectedNames) {
        PlatformIO selector = platformBuilder.buildInput();
        DeployedModuleBuilder moduleBuilder = platformBuilder.findDeployedModuleBuilder(moduleName);

        PropertiesIO output = platformClient.getProperties(selector, moduleBuilder.buildPropertiesPath());

        assertThat(output.getValuedProperties()).hasSize(expectedNames.size())
                .allSatisfy(valueProperty -> assertThat(valueProperty.getName()).isIn(expectedNames));
    }
}
