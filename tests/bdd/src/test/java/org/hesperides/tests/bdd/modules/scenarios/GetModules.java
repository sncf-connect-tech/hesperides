package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.ModuleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.StepHelper.*;
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

        When("^I( try to)? get the module detail(?: for a module version \"(.*)\")?$", (String tryTo, String moduleType) -> {
            if (StringUtils.isNotEmpty(moduleType)) {
                moduleBuilder.withModuleType(moduleType);
            }
            responseEntity = moduleClient.get(moduleBuilder.build(), moduleBuilder.getVersionType(), getResponseType(tryTo, ModuleIO.class));
        });

        Then("^the module detail is successfully retrieved$", () -> {
            assertOK(responseEntity);
            ModuleIO expectedModule = moduleBuilder.withVersionId(1).build();
            ModuleIO actualModule = (ModuleIO) responseEntity.getBody();
            assertEquals(expectedModule, actualModule);
        });

        Then("^the module is not found$", () -> {
            assertNotFound(responseEntity);
        });

        Then("^the request is rejected with a bad request error$", () -> {
            assertBadRequest(responseEntity);
        });
    }
}
