package backup.modules.scenarios;

import org.hesperides.test.bdd.modules.OldModuleClient;
import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.OldModuleBuilder;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyModules extends HesperidesScenario implements En {

    @Autowired
    private OldModuleClient moduleClient;
    @Autowired
    private OldModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    public CopyModules() {

        When("^I( try to)? create a copy of this module" +
                "( without specifying the version of the source module)?" +
                "( without specifying whether it is a workingcopy)?$", (
                String tryTo,
                String unknownSrcVersion,
                String unknownSrcWorkingCopy) -> {
            if (!isBlank(unknownSrcWorkingCopy)) {
                moduleBuilder.withVersionType(null);
            }
            if (!isBlank(unknownSrcVersion)) {
                moduleBuilder.withVersion(null);
            }
            testContext.setResponseEntity(copy("1.0.1", getResponseType(tryTo, ModuleIO.class)));
        });

        Given("^a copy of this module in version (.+)$", (String version) -> {
            copy(version, ModuleIO.class);
        });

        Given("^a copy of this module changing the name to \"([^\"]*)\"$", (String name) -> {
            ModuleIO existingModule = moduleBuilder.build();
            ModuleIO newModule = moduleBuilder.withVersionType(VersionType.WORKINGCOPY).withName(name).build();
            moduleClient.copy(existingModule, newModule, ModuleIO.class);
        });

        When("^I try to create a copy of this module, using the same key$", () -> {
            testContext.setResponseEntity(copy(moduleBuilder.build().getVersion(), String.class));
        });

        Then("^the module is successfully duplicated$", () -> {
            assertCreated();
            ModuleIO expectedModule = moduleBuilder.build();
            ModuleIO actualModule = testContext.getResponseBody(ModuleIO.class);
            assertEquals(expectedModule, actualModule);

            // Vérifie la liste des templates
            // Seul le namespace est différent
            String expectedNamespace = moduleBuilder.getNamespace();
            List<PartialTemplateIO> expectedTemplates = moduleClient.getTemplates(moduleBuilder.build())
                    .stream()
                    .map(expectedTemplate -> new PartialTemplateIO(expectedTemplate.getName(), expectedNamespace, expectedTemplate.getFilename(), expectedTemplate.getLocation()))
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualTemplates = moduleClient.getTemplates(actualModule);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the version type of the duplicated module is working copy$", () -> {
            ModuleIO moduleOutput = testContext.getResponseBody(ModuleIO.class);
            assertTrue(moduleOutput.getIsWorkingCopy());
        });

        Then("^the model of the module is the same$", () -> {
            testContext.setResponseEntity(moduleClient.getModel(moduleBuilder.build(), ModelOutput.class));
            assertOK();
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
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
        ModuleIO existingModule = moduleBuilder.build();
        moduleBuilder.withVersionType(VersionType.WORKINGCOPY).withVersion(newVersion);
        return moduleClient.copy(existingModule, moduleBuilder.build(), responseType);
    }
}
