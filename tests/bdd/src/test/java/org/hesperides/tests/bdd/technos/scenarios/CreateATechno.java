package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.inputs.RightsInput;
import org.hesperides.presentation.inputs.TechnoInput;
import org.hesperides.presentation.inputs.TemplateInput;
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
            technoInput = new TechnoInput(
                    "technoName",
                    "technoVersion",
                    true,
                    new TemplateInput(
                            "fichierTest",
                            "test.json",
                            "/home/test",
                            "{test:test}",
                            new RightsInput(
                                    new RightsInput.FileRights(null, null, null),
                                    new RightsInput.FileRights(null, null, null),
                                    null
                            ), -1L));
        });

        When("^creating a new techno$", () -> {
            technoLocation = rest.postForLocationReturnAbsoluteURI(
                    String.format("/templates/packages/%s/%s/workingcopy/templates", technoInput.getName(), technoInput.getVersion()),
                    technoInput.getTemplate());
        });


        Then("^the techno is successfully created$", () -> {
            ResponseEntity<TemplateView> responseEntity = rest.getForEntity(technoLocation, TemplateView.class);
            assertEquals(1L, responseEntity.getBody().getVersionId().longValue());
            assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
        });
    }

}
