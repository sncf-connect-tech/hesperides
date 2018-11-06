package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;
import static org.junit.Assert.assertEquals;

public class GetTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;

    public GetTechnos() {

        Given("^a techno that doesn't exist$", () -> {
            technoBuilder.withName("nope");
        });

        When("^I( try to)? get the techno detail$", (String tryTo) -> {
            testContext.responseEntity = technoClient.get(technoBuilder.build(), getResponseType(tryTo, TechnoIO.class));
        });

        Then("^the techno detail is successfully retrieved$", () -> {
            assertOK();
            TechnoIO expectedTechno = technoBuilder.build();
            TechnoIO actualTechno = (TechnoIO) testContext.getResponseBody();
            assertEquals(expectedTechno, actualTechno);
        });
    }
}
