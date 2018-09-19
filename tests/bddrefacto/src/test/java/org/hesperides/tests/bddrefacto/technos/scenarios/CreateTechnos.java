package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertConflict;
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
            if (StringUtils.isNotEmpty(withProperties)) {
                templateBuilder.withProperty("foo").withProperty("bar");
            }
            technoClient.create(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
        });

        Given("^a techno to create(?: with the same name and version)?$", () -> {
            technoBuilder.reset();
        });

        When("^I( try to)? create this techno$", (final String tryTo) -> {
            responseEntity = technoClient.create(templateBuilder.build(), technoBuilder.build(), StepHelper.getResponseType(tryTo, TemplateIO.class));
        });

        Then("^the techno is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            String expectedNamespace = technoBuilder.getNamespace();
            TemplateIO excpedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            assertEquals(excpedTemplate, actualTemplate);
        });

        Then("^the techno creation is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
        });
    }
}
