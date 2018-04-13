package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.presentation.controllers.RightsInput;
import org.hesperides.presentation.controllers.TechnoInput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CreateATechno extends CucumberSpringBean implements En {
    private TechnoInput technoInput;
    private URI technoLocation;

    public CreateATechno() {
        Given("^a techno to create$", () -> {
            technoInput = new TechnoInput("testTechno", "test.json", "/home/test", "{test:test}", -1L,
                    new RightsInput(
                            new RightsInput.FileRights(null, null, null),
                            new RightsInput.FileRights(null, null, null),
                            null
                    ));
        });

        When("^creating a new techno$", () -> {
            technoLocation = rest.postForLocationReturnAbsoluteURI("/technos", technoInput);
        });


        Then("^the techno is successfully created$", () -> {
            ResponseEntity<TechnoView> responseEntity = rest.getForEntity(technoLocation, TechnoView.class);
            assertEquals(1L, responseEntity.getBody().getVersionId().longValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }

}
