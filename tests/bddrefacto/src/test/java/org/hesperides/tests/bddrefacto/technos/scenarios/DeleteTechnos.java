package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertNotFound;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;

public class DeleteTechnos implements En {

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity responseEntity;

    public DeleteTechnos() {

        When("^I( try to)? delete this techno$", (final String tryTo) -> {
            responseEntity = technoClient.delete(technoBuilder.build(), StepHelper.getResponseType(tryTo, ResponseEntity.class));
        });
        ;

        Then("^the techno is successfully deleted$", () -> {
            assertOK(responseEntity);
            responseEntity = technoClient.get(technoBuilder.build(), String.class);
            assertNotFound(responseEntity);
        });

        Then("^the techno deletion is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
