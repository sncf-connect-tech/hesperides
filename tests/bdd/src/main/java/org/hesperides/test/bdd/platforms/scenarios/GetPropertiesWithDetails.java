package org.hesperides.test.bdd.platforms.scenarios;

import cucumber.api.DataTable;
import cucumber.api.java8.En;
import org.hesperides.core.domain.platforms.entities.properties.PropertyWithDetails;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertyWithDetailsIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.hesperides.test.bdd.platforms.PlatformHistory;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetPropertiesWithDetails extends HesperidesScenario implements En {
    @Autowired
    private PlatformClient platformClient;
    @Autowired
    private PlatformBuilder platformBuilder;
    @Autowired
    private PlatformHistory platformHistory;
    @Autowired
    private DeployedModuleBuilder deployedModuleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetPropertiesWithDetails() {

        When("^I get the properties with details of this platforms$", () -> {
            platformClient.getPropertiesWithDetails(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
        });

        Then("^the properties with theirs details are successfully retrieved$", () -> {
            assertOK();
            PropertiesIO expectedModuleProperties = platformBuilder.buildPropertiesWithDetails();
            PropertiesIO actualModuleProperties = testContext.getResponseBody();
            assertEquals(expectedModuleProperties, actualModuleProperties);
        });

        When("^I get the platform properties with details for this module$", () -> {
            platformClient.getPropertiesWithDetails(platformBuilder.buildInput(), deployedModuleBuilder.buildPropertiesPath());
        });

        Then("^the properties with details and its contain are successfully retrieved$", () -> {
            assertOK();
        });

        Then("^the properties details match these values$", (DataTable data) -> {
            List<PropertyWithDetailsIO> providedProperties = new ArrayList<>(data.asList(PropertyWithDetailsIO.class));
            // the true boolean parameter to indicate that : we want forced the blank properties values to null
            List<PropertyWithDetailsIO> expectedModuleProperties = PropertyWithDetailsIO.replaceBlankPropertiesWithNull(providedProperties);
            PropertiesIO actualModuleProperties = testContext.getResponseBody();
            assertEquals(expectedModuleProperties, actualModuleProperties.getValuedProperties());
        });
    }
}
