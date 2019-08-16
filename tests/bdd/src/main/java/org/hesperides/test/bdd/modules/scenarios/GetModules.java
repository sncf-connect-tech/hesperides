package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class GetModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModules() {

        Given("^a module that doesn't exist$", () -> moduleBuilder.withName("doesn-t-exist"));

        When("^I( try to)? get the module detail(?: for a module type \"(.*)\")?( with the wrong letter case)?$", (
                String tryTo, String moduleType, String withWrongLetterCase) -> {

            if (isNotEmpty(moduleType)) {
                moduleBuilder.withVersionType(moduleType);
            }
            if (isNotEmpty(withWrongLetterCase)) {
                moduleBuilder.withName(moduleBuilder.getName().toUpperCase());
            }
            moduleClient.getModule(moduleBuilder.build(), moduleBuilder.getVersionType(), tryTo);
        });

        When("^I get the modules names$", () -> moduleClient.getModuleNames());

        When("^I get the module types$", () -> {
            ModuleIO module = moduleBuilder.build();
            moduleClient.getModuleTypes(module.getName(), module.getVersion());
        });

        When("^I get the module versions$", () -> moduleClient.getModuleVersions("new-module"));

        Then("^the module detail is successfully retrieved$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody(ModuleIO.class);
            assertEquals(expectedModule, actualModule);
        });
    }
}
