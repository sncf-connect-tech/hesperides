package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class GetModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    public GetModules() {

        Given("^a module that doesn't exist$", () -> {
            moduleBuilder.withName("nope");
        });

        When("^I( try to)? get the module detail(?: for a module type \"(.*)\")?( with the wrong letter case)?$", (String tryTo, String moduleType, String withWrongLetterCase) -> {
            if (StringUtils.isNotEmpty(moduleType)) {
                moduleBuilder.withModuleType(moduleType);
            }
            ModuleIO moduleInput = moduleBuilder.build();
            if (StringUtils.isNotEmpty(withWrongLetterCase)) {
                moduleInput = new ModuleBuilder().withName(moduleBuilder.getName().toUpperCase()).build();
            }
            testContext.responseEntity = moduleClient.get(moduleInput, moduleBuilder.getVersionType(), getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module detail is successfully retrieved$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.withVersionId(1).build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });
    }
}
