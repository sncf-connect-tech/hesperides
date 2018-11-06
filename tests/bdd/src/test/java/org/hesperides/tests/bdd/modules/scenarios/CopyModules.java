package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;

    public CopyModules() {

        When("^I( try to)? create a copy of this module( without specifying the version of the source module)?( without specifying whether it is a workingcopy)?$", (String tryTo, String unknownSrcVersion, String unknownSrcWorkingCopy) -> {
            if (!isBlank(unknownSrcWorkingCopy)) {
                moduleBuilder.withModuleType(null);
            }
            if (!isBlank(unknownSrcVersion)) {
                moduleBuilder.withVersion(null);
            }
            testContext.responseEntity = copy("1.0.1", getResponseType(tryTo, ModuleIO.class));
        });

        When("^I try to create a copy of this module, using the same key$", () -> {
            testContext.responseEntity = copy(moduleBuilder.build().getVersion(), String.class);
        });

        Then("^the module is successfully duplicated$", () -> {
            assertCreated();
            ModuleBuilder expectedModuleBuilder = new ModuleBuilder().withTechno(technoBuilder.build()).withVersionId(1).withVersion("1.0.1");
            ModuleIO expectedModule = expectedModuleBuilder.build();
            ModuleIO actualModule = (ModuleIO) testContext.getResponseBody();
            assertEquals(expectedModule, actualModule);

            // Vérifie la liste des templates
            // Seul le namespace est différent
            String expectedNamespace = expectedModuleBuilder.getNamespace();
            List<PartialTemplateIO> expectedTemplates = moduleClient.getTemplates(moduleBuilder.build())
                    .stream()
                    .map(expectedTemplate -> new PartialTemplateIO(expectedTemplate.getName(), expectedNamespace, expectedTemplate.getFilename(), expectedTemplate.getLocation()))
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualTemplates = moduleClient.getTemplates(actualModule);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the version type of the duplicated module is working copy$", () -> {
            ModuleIO techoOutput = (ModuleIO) testContext.getResponseBody();
            assertTrue(techoOutput.isWorkingCopy());
        });

        Then("^the model of the module is the same$", () -> {
            testContext.responseEntity = moduleClient.getModel(moduleBuilder.build(), ModelOutput.class);
            assertOK();
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = (ModelOutput) testContext.getResponseBody();
            assertEquals(expectedModel, actualModel);
        });

        Then("^the module copy is rejected with a not found error$", () -> {
            assertNotFound();
        });

        Then("^the module copy is rejected with a conflict error$", () -> {
            assertConflict();
        });

        Then("^the module copy is rejected with a bad request error$", () -> {
            assertBadRequest();
        });
    }

    private ResponseEntity copy(String newVersion, Class responseType) {
        ModuleIO newModuleInput = new ModuleBuilder().withVersion(newVersion).build();
        return moduleClient.copy(moduleBuilder.build(), moduleBuilder.isWorkingCopy(), newModuleInput, responseType);
    }

}
