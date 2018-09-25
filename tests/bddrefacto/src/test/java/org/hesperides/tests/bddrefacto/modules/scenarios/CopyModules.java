package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.templatecontainers.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyModules implements En {

    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity responseEntity;

    public CopyModules() {

        When("^I( try to)? create a copy of this module$", (final String tryTo) -> {
            responseEntity = copy("1.0.1", StepHelper.getResponseType(tryTo, ModuleIO.class));
        });

        When("^I try to create a copy of this module, using the same key$", () -> {
            responseEntity = copy(moduleBuilder.build().getVersion(), String.class);
        });

        Then("^the module is successfully duplicated$", () -> {
            assertCreated(responseEntity);
            ModuleBuilder expectedModuleBuilder = new ModuleBuilder().withVersionId(1).withVersion("1.0.1");
            ModuleIO expectedModule = expectedModuleBuilder.build();
            ModuleIO actualModule = (ModuleIO) responseEntity.getBody();
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
            ModuleIO techoOutput = (ModuleIO) responseEntity.getBody();
            assertTrue(techoOutput.isWorkingCopy());
        });

        Then("^the model of the module is the same$", () -> {
            responseEntity = moduleClient.getModel(moduleBuilder.build(), ModelOutput.class);
            assertOK(responseEntity);
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = (ModelOutput) responseEntity.getBody();
            assertEquals(expectedModel, actualModel);
        });

        Then("^the module copy is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });

        Then("^the module copy is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }

    private ResponseEntity copy(final String newVersion, Class responseType) {
        ModuleIO newModuleInput = new ModuleBuilder().withVersion(newVersion).build();
        return moduleClient.copy(moduleBuilder.build(), newModuleInput, responseType);
    }
}
