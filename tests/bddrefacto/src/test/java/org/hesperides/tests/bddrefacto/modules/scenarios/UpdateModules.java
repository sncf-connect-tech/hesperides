package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class UpdateModules implements En {

    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private ModuleClient moduleClient;

    private ResponseEntity responseEntity;

    public UpdateModules() {

        Given("^the module is outdated$", () -> {
            moduleBuilder.withVersionId(0);
        });

        Given("^this techno is associated to this module$", () -> {
            moduleBuilder.withTechno(technoBuilder.build());
        });

        When("^I( try to)? update this module$", (final String tryTo) -> {
            responseEntity = moduleClient.update(moduleBuilder.build(), StepHelper.getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module is successfully updated$", () -> {
            assertOK(responseEntity);
            ModuleIO excpectedModule = moduleBuilder.withVersionId(2).build();
            ModuleIO actualModule = (ModuleIO) responseEntity.getBody();
            assertEquals(excpectedModule, actualModule);
        });

        Then("^the module update is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });

        Then("^the module update is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
        });

        Then("^the module update is rejected with a bad request error$", () -> {
            assertBadRequest(responseEntity);
        });
    }
}
