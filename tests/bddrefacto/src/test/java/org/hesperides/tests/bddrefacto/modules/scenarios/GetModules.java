package org.hesperides.tests.bddrefacto.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bddrefacto.modules.ModuleBuilder;
import org.hesperides.tests.bddrefacto.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class GetModules implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;

    private ResponseEntity responseEntity;

    public GetModules() {

        Given("^a module that doesn't exist$", () -> {
            moduleBuilder.withName("nope");
        });

        When("^I( try to)? get the module detail$", (final String tryTo) -> {
            responseEntity = moduleClient.get(moduleBuilder.build(), getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module detail is successfully retrieved$", () -> {
            assertOK(responseEntity);
            ModuleIO expectedModule = moduleBuilder.withVersionId(1).build();
            ModuleIO actualModule = (ModuleIO) responseEntity.getBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^the module is not found$", () -> {
            assertNotFound(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }
}
