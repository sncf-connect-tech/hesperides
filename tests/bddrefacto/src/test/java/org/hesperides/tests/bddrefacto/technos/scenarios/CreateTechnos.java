package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateTechnos implements En {
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    private ResponseEntity responseEntity;

    public CreateTechnos() {

        Given("^an existing techno$", () -> {
            createTechno(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
        });

        Given("^a techno to create(?: with the same name and version)?$", () -> {
            templateBuilder = new TemplateBuilder();
            technoBuilder = new TechnoBuilder();
        });

        When("^I create this techno$", () -> {
            responseEntity = createTechno(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
        });

        When("^I try to create this techno$", () -> {
            responseEntity = createTechno(templateBuilder.build(), technoBuilder.build(), String.class);
        });

        Then("^the techno is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            String expectedNamespace = getExpectedNamespace(technoBuilder.build());
            TemplateIO excpedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            TemplateAssertions.assertTemplate(excpedTemplate, actualTemplate);
        });

        Then("^the techno is rejected with a 409 error$", () -> {
            assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        });
    }

    private ResponseEntity createTechno(TemplateIO templateInput, TechnoIO technoInput, Class responseType) {
        return testRestTemplate.postForEntity(
                "/templates/packages/{technoName}/{technoVersion}/workingcopy/templates",
                templateInput,
                responseType,
                technoInput.getName(),
                technoInput.getVersion());
    }

    private String getExpectedNamespace(TechnoIO technoInput) {
        return "packages#" + technoInput.getName() + "#" + technoInput.getVersion() + "#" + (technoInput.isWorkingCopy() ? "WORKINGCOPY" : "RELEASE");
    }
}
