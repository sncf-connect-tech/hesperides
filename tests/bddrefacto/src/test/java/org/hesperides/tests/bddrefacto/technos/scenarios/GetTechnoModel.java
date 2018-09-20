package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.ModelBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.PropertyBuilder;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertNotFound;
import static org.hesperides.tests.bddrefacto.commons.StepHelper.assertOK;
import static org.junit.Assert.assertEquals;

public class GetTechnoModel implements En {

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private PropertyBuilder propertyBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity responseEntity;

    public GetTechnoModel() {

        Given("^a techno template with properties$", () -> {
            templateBuilder.withName("new-template");

            propertyBuilder.reset().withName("foo");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.withProperty(propertyBuilder.toString());

            propertyBuilder.reset().withName("bar");
            modelBuilder.withProperty(propertyBuilder.build());
            templateBuilder.withProperty(propertyBuilder.toString());

            technoClient.addTemplate(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
        });

        When("^I( try to)? get the model of this techno$", (final String tryTo) -> {
            responseEntity = technoClient.getModel(technoBuilder.build(), StepHelper.getResponseType(tryTo, ModelOutput.class));
        });

        Then("^the model of this techno contains the properties$", () -> {
            assertOK(responseEntity);
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = (ModelOutput) responseEntity.getBody();
            assertEquals(expectedModel, actualModel);
        });

        Then("^the techno model if not found$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
