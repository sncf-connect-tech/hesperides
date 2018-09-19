package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoAssertions;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            TechnoAssertions.assertTechno(technoBuilder.build(), (TechnoIO) responseEntity.getBody());
        });

        Then("^the techno is not found$", () -> {
            assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }
}
