package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.tests.bddrefacto.technos.TechnoAssertions;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class GetTechnos implements En {
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private TechnoBuilder technoBuilder;

    private ResponseEntity responseEntity;

    public GetTechnos() {

        Given("^a techno that doesn't exist$", () -> {
            technoBuilder.withName("nope");
        });

        When("^I get the techno detail$", () -> {
            responseEntity = testRestTemplate.getForEntity(getTechnoUrl(), TechnoIO.class);
        });

        When("^I try to get the techno detail$", () -> {
            responseEntity = testRestTemplate.getForEntity(getTechnoUrl(), String.class);
        });

        Then("^the techno detail is successfully retrieved$", () -> {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            TechnoAssertions.assertTechno(technoBuilder.build(), (TechnoIO) responseEntity.getBody());
        });

        Then("^I get a 404 error$", () -> {
            assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }

    private String getTechnoUrl() {
        TemplateContainer.Key technoKey = technoBuilder.getTechnoKey();
        return String.format("/templates/packages/%s/%s/%s", technoKey.getName(), technoKey.getVersion(), technoKey.getVersionType());
    }
}
