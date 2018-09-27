package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.ModelBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.PropertyBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class CreateTechnos implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity responseEntity;

    public CreateTechnos() {

        Given("^an existing techno( with properties)?$", (String withProperties) -> {
            if (StringUtils.isNotEmpty(withProperties)) {
                addPropertyToBuilders("techno-foo");
                addPropertyToBuilders("techno-bar");
            }
            technoClient.create(templateBuilder.build(), technoBuilder.build());
        });

        Given("^a techno to create(?: with the same name and version)?$", () -> {
            technoBuilder.reset();
        });

        When("^I( try to)? create this techno$", (final String tryTo) -> {
            responseEntity = technoClient.create(templateBuilder.build(), technoBuilder.build(), getResponseType(tryTo, TemplateIO.class));
        });

        Then("^the techno is successfully created$", () -> {
            assertCreated(responseEntity);
            String expectedNamespace = technoBuilder.getNamespace();
            TemplateIO excpectedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            assertEquals(excpectedTemplate, actualTemplate);
        });

        Then("^the techno creation is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
        });
    }

    private void addPropertyToBuilders(String name) {
        propertyBuilder.reset().withName(name);
        modelBuilder.withProperty(propertyBuilder.build());
        templateBuilder.withContent(propertyBuilder.toString());
        technoBuilder.withProperty(propertyBuilder.build());
    }
}
