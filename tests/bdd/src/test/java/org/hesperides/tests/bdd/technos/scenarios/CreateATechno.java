package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.hesperides.tests.bdd.templatecontainers.TemplateAssertions;
import org.hesperides.tests.bdd.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateATechno implements En {
    @Autowired
    private TechnoContext technoContext;
    @Autowired
    private TemplateBuilder templateBuilder;

    @Autowired
    private TechnoBuilder technoBuilder;

    private TechnoIO technoInput;
    private TemplateIO templateInput;
    private ResponseEntity<TemplateIO> response;

    public CreateATechno() {
        Given("^a techno to create$", () -> {
            templateInput = new TemplateBuilder().build();
            technoInput = new TechnoBuilder().build();
        });

        When("^creating a new techno$", () -> {
            response = technoContext.createTechno(technoInput, templateInput);
        });


        Then("^the techno is successfully created$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TemplateIO templateOutput = response.getBody();
            TemplateAssertions.assertTemplateAgainstDefaultValues(templateOutput, technoContext.getNamespace(), 1);
        });

        Given("^a template property with required and default value annotations$", () -> {
            templateBuilder.withDefaultAndRequiredProperty();
        });

        Given("^a techno template with properties$", () -> {
            templateBuilder.withProperty("foo").withProperty("bar");
        });

//        Given("^a techno for this module(?: with this information)?$", () -> {
        Given("^an existing techno for this module$", () -> {
            TechnoIO techno = technoBuilder.build();
            technoContext.createTechno(techno, templateBuilder.build());

            // TODO: Faire la vérification du 200


//            sharedContext.getBuilder(ModuleBuilder.class).withTechno(techno);
        });

        When("^I create a template in techno with this property$", () -> {
            response = technoContext.createTechno(technoBuilder.build(), templateBuilder.build());
        });

        Then("^the creation of the techno template is rejected$", () -> {
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        });
    }
}
