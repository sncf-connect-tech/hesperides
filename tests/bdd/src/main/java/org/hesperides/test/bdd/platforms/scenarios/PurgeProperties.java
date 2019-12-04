package org.hesperides.test.bdd.platforms.scenarios;

import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import cucumber.api.DataTable;
import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PurgeProperties extends HesperidesScenario implements En {

    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;

    public PurgeProperties() {
        When("I purge unneeded properties(?: on path \"([^\"]+)\")?", (String path) -> {
            PlatformIO input = platformBuilder.buildInput();
            platformClient.cleanUnusedProperties(input, path);
        });

        Then("module \"([^\"]+)\" contains only the following properties", (String moduleName, DataTable propertyNames) -> {
            List<String> expectedNames = propertyNames.asList(String.class);

            PlatformIO selector = platformBuilder.buildInput();
            DeployedModuleBuilder moduleBuilder = platformBuilder.findDeployedModuleBuilderByName(moduleName);

            PropertiesIO output = platformClient.getProperties(selector, moduleBuilder.buildPropertiesPath());

            assertThat(output.getValuedProperties()).hasSize(expectedNames.size())
                    .allSatisfy(valueProperty -> assertThat(valueProperty.getName()).isIn(expectedNames));
        });
    }
}
