package org.hesperides.test.bdd.modules.scenarios;

import io.cucumber.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.ModuleKeyOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class GetModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private ModuleHistory moduleHistory;

    public GetModules() {

        Given("^a module that doesn't exist$", () -> moduleBuilder.withName("doesn-t-exist"));

        When("^I( try to)? get the module detail(?: for a module type \"([^\"]*)\")?( with the wrong letter case)?$", (
                String tryTo, String moduleType, String wrongLetterCase) -> {

            if (isNotEmpty(moduleType)) {
                moduleBuilder.withVersionType(moduleType);
            }
            String moduleName = isNotEmpty(wrongLetterCase) ? moduleBuilder.getName().toUpperCase() : moduleBuilder.getName();
            moduleClient.getModule(moduleBuilder.buildWithName(moduleName), moduleBuilder.getVersionType(), tryTo);
        });

        When("^I get the modules name$", () -> moduleClient.getModuleNames());

        When("^I get the module types$", () -> {
            ModuleIO module = moduleBuilder.build();
            moduleClient.getModuleTypes(module.getName(), module.getVersion());
        });

        When("^I get the module versions$", () -> moduleClient.getModuleVersions(moduleBuilder.getName()));

        When("^I get the modules using this techno$", () -> {
            moduleClient.getModulesUsingTechno(technoBuilder.build());
        });

        Then("^the module detail is successfully retrieved$", () -> {
            assertOK();
            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^a list containing workingcopy and release is returned$", () -> {
            assertOK();
            String[] body = testContext.getResponseBodyAsArray();
            assertEquals(2, body.length);
            assertEquals("workingcopy", body[0]);
            assertEquals("release", body[1]);
        });

        Then("^a list containing workingcopy is returned$", () -> {
            assertOK();
            String[] body = testContext.getResponseBodyAsArray();
            assertEquals(1, body.length);
            assertEquals("workingcopy", body[0]);
        });

        Then("^the modules using this techno are successfully retrieved", () -> {
            assertOK();
            List<ModuleKeyOutput> expectedModules = moduleHistory.getModuleBuilders().stream()
                    .map(ModuleBuilder::buildModuleKeyOutput)
                    .collect(Collectors.toList());
            List<ModuleKeyOutput> actualModules = testContext.getResponseBodyAsList();
            assertEquals(expectedModules, actualModules);
        });
    }
}
