package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.tests.bdd.commons.tools.HesperidesTestRestTemplate;
import org.hesperides.tests.bdd.technos.TechnoAssertions;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetTechnoInfo implements En {

    @Autowired
    private TechnoContext technoContext;
    @Autowired
    private HesperidesTestRestTemplate rest;

    private ResponseEntity<TechnoIO> response;

    public GetTechnoInfo() {

        When("^retrieving the techno's info$", () -> {
            response = retrieveExistingTechno();
        });

        Then("^the techno's info is retrieved$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            TechnoIO technoOutput = response.getBody();
            TechnoAssertions.assertTechnoAgainstDefaultValues(technoOutput);
        });
    }

    private ResponseEntity<TechnoIO> retrieveExistingTechno() {
        return rest.getTestRest().getForEntity(technoContext.getTechnoURI(), TechnoIO.class);
    }

}
