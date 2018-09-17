package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.modules.ModuleAssertions;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CopyAModule implements En {

    @Autowired
    private ModuleContext moduleContext;
    @Autowired
    private TechnoContext technoContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    private ResponseEntity<ModuleIO> response;

    public CopyAModule() {

        Given("^the techno is attached to the module$", () -> {
            TechnoIO technoInput = new TechnoBuilder().withKey(technoContext.getTechnoKey()).build();
            ModuleIO moduleInput = new ModuleBuilder()
                    .withTechno(technoInput)
                    .withVersionId(1)
                    .build();
            moduleContext.updateModule(moduleInput);
        });

        When("^I create a copy of this module$", () -> {
            ModuleIO moduleInput = new ModuleBuilder()
                    .withName("module-copy")
                    .withVersion("1.0.1")
                    .build();
            response = copyModule(moduleInput);
        });

        Then("^the module is successfully and completely duplicated$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            ModuleIO actualModuleOutput = response.getBody();
            TechnoIO technoInput = new TechnoBuilder().withKey(technoContext.getTechnoKey()).build();
            ModuleIO expectedModuleOutput = new ModuleBuilder()
                    .withName("module-copy")
                    .withVersion("1.0.1")
                    .withTechno(technoInput)
                    .build();
            ModuleAssertions.assertModule(expectedModuleOutput, actualModuleOutput, 1L);
        });

        Then("^the model of the module is also duplicated$", () -> {
            ResponseEntity<ModelOutput> modelResponse = rest.getTestRest().getForEntity(moduleContext.getModuleURI(
                    new Module.Key("module-copy", "1.0.1", TemplateContainer.VersionType.workingcopy)) + "/model", ModelOutput.class);
            assertEquals(HttpStatus.OK, modelResponse.getStatusCode());
            ModelOutput modelOutput = modelResponse.getBody();
            assertEquals(2, modelOutput.getProperties().size());
        });
    }

    private ResponseEntity<ModuleIO> copyModule(ModuleIO moduleInput) {
        TemplateContainer.Key moduleKey = moduleContext.getModuleKey();
        return rest.getTestRest().postForEntity("/modules?from_module_name={moduleName}&from_module_version={moduleVersion}&from_is_working_copy={isWorkingCopy}",
                moduleInput, ModuleIO.class,
                moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy());
    }

    //TODO Assert templates
}
