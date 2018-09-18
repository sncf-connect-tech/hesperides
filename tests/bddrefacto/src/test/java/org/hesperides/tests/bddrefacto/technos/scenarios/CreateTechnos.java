package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateTechnos implements En {

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity responseEntity;

    public CreateTechnos() {

        Given("^an existing techno( with properties)?$", (String withProperties) -> {
            technoBuilder = new TechnoBuilder();
            templateBuilder = new TemplateBuilder();

            if (StringUtils.isNotEmpty(withProperties)) {
                templateBuilder.withProperty("foo").withProperty("bar");
            }

            technoClient.create(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
        });

        Given("^a techno to create(?: with the same name and version)?$", () -> {
            templateBuilder = new TemplateBuilder();
            technoBuilder = new TechnoBuilder();
        });

        When("^I create this techno$", () -> {
            responseEntity = technoClient.create(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
        });

        When("^I try to create this techno$", () -> {
            responseEntity = technoClient.create(templateBuilder.build(), technoBuilder.build(), String.class);
        });

        Then("^the techno is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            String expectedNamespace = getExpectedNamespace(technoBuilder.build());
            TemplateIO excpedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            TemplateAssertions.assertTemplate(excpedTemplate, actualTemplate);
        });

        Then("^the techno creation is rejected with a conflict error$", () -> {
            assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        });
    }

    private String getExpectedNamespace(TechnoIO technoInput) {
        return "packages#" + technoInput.getName() + "#" + technoInput.getVersion() + "#" + (technoInput.isWorkingCopy() ? "WORKINGCOPY" : "RELEASE");
    }
}
