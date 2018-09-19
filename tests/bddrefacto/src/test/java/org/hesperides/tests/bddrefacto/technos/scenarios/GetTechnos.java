package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertNotFound;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class GetTechnos implements En {

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity responseEntity;

    public GetTechnos() {

        Given("^a techno that doesn't exist$", () -> {
            technoBuilder.withName("nope");
        });

        When("^I( try to)? get the techno detail$", (final String tryTo) -> {
            responseEntity = technoClient.get(technoBuilder.build(), StepHelper.getResponseType(tryTo, TechnoIO.class));
        });

        Then("^the techno detail is successfully retrieved$", () -> {
            assertOK(responseEntity);
            TechnoIO expectedTechno = technoBuilder.build();
            TechnoIO actualTechno = (TechnoIO) responseEntity.getBody();
            assertEquals(expectedTechno, actualTechno);
        });

        Then("^the techno is not found$", () -> {
            assertNotFound(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }
}
