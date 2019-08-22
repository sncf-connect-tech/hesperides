package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class CopyModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;

    public CopyModules() {

        Given("^a copy of this module(?: in version \"(.*)\")?(?: using the name \"(.*)\")?$", (String copyVersion, String copyName) -> {
            ModuleIO existingModule = moduleBuilder.build();
            moduleBuilder.withVersion(isNotEmpty(copyVersion) ? copyVersion : "1.1");
            if (isNotEmpty(copyName)) {
                moduleBuilder.withName(copyName);
            }
            copyModule(existingModule, null);
        });

        When("^I( try to)? create a copy of this module" +
                "( without specifying the version of the source module)?" +
                "( without specifying whether it is a workingcopy)?" +
                "(, using the same key)?$", (
                String tryTo,
                String unknownSourceModuleVersion,
                String unknownSourceModuleVersionType,
                String usingTheSameKey) -> {

            ModuleIO existingModule = moduleBuilder.build();

            if (isNotEmpty(unknownSourceModuleVersion)) {
                moduleBuilder.withVersion(null);
            } else if (isEmpty(usingTheSameKey)) {
                moduleBuilder.withVersion("1.1");
            }

            if (isNotEmpty(unknownSourceModuleVersionType)) {
                moduleBuilder.withVersionType(null);
            }

            copyModule(existingModule, tryTo);
        });

        Then("^the module is successfully (duplicated|released)?$", (String duplicatedOrReleased) -> {
            if ("duplicated".equals(duplicatedOrReleased)) {
                assertCreated();
            } else {
                assertOK();
            }

            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);

            List<PartialTemplateIO> expectedTemplates = moduleBuilder.getTemplateBuilders()
                    .stream()
                    .map(TemplateBuilder::buildPartialTemplate)
                    .collect(Collectors.toList());

            List<PartialTemplateIO> actualTemplates = moduleClient.getTemplates(actualModule);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the model of the duplicated module is the same$", () -> {
            moduleClient.getModel(moduleBuilder.build());
            assertOK();
            ModelOutput expectedModel = moduleHistory.getFirstModuleBuilder().buildPropertiesModel();
            ModelOutput actualModel = testContext.getResponseBody();
            assertEquals(expectedModel, actualModel);
        });

        Then("^the module copy is rejected with a not found error$", this::assertNotFound);

        Then("^the module copy is rejected with a bad request error$", this::assertBadRequest);

        Then("^the module copy is rejected with a conflict error$", this::assertConflict);
    }

    private void copyModule(ModuleIO existingModule, String tryTo) {
        moduleClient.copyModule(existingModule, moduleBuilder.build(), tryTo);
        if (isEmpty(tryTo)) {
            moduleBuilder.withVersionId(1);
            // Les templates sont identiques sauf pour le namespace
            moduleBuilder.updateTemplatesNamespace();
            moduleHistory.addModuleBuilder(moduleBuilder);
        }
    }
}
