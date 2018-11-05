package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.tests.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;
import static org.junit.Assert.assertEquals;

public class CreateTechnos extends HesperidesScenario implements En {

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

    public CreateTechnos() {

        Given("^an existing techno( with properties)?( (?:and|with) global properties)?$", (String withProperties, String withGlobalProperties) -> {
            if (StringUtils.isNotEmpty(withProperties)) {
                addPropertyToBuilders("techno-foo");
                addPropertyToBuilders("techno-bar");
            }
            if (StringUtils.isNotEmpty(withGlobalProperties)) {
                addPropertyToBuilders("global-techno-foo");
                addPropertyToBuilders("global-techno-bar");
            }
            technoClient.create(templateBuilder.build(), technoBuilder.build());
        });

        Given("^a techno to create(?: with the same name and version)?$", () -> {
            technoBuilder.reset();
        });

        When("^I( try to)? create this techno$", (String tryTo) -> {
            responseEntity = technoClient.create(templateBuilder.build(), technoBuilder.build(), getResponseType(tryTo, TemplateIO.class));
        });

        Then("^the techno is successfully created$", () -> {
            assertCreated();
            String expectedNamespace = technoBuilder.getNamespace();
            TemplateIO expectedTemplate = templateBuilder.withNamespace(expectedNamespace).withVersionId(1).build();
            TemplateIO actualTemplate = (TemplateIO) responseEntity.getBody();
            assertEquals(expectedTemplate, actualTemplate);
        });

        Then("^the techno creation is rejected with a conflict error$", () -> {
            assertConflict();
        });
    }

    private void addPropertyToBuilders(String name) {
        propertyBuilder.reset().withName(name);
        modelBuilder.withProperty(propertyBuilder.build());
        templateBuilder.withContent(propertyBuilder.toString());
        technoBuilder.withProperty(propertyBuilder.build());
    }
}
