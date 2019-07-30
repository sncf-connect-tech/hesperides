package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class DeleteTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    public DeleteTechnos() {

        When("^I( try to)? delete this techno$", (String tryTo) -> {
            testContext.setResponseEntity(technoClient.delete(technoBuilder.build(), getResponseType(tryTo, ResponseEntity.class)));
            moduleBuilder.removeTechno(technoBuilder.build());
            modelBuilder.removeProperties(technoBuilder.getProperties());
        });

        Then("^the techno is successfully deleted$", () -> {
            assertOK();
            testContext.setResponseEntity(technoClient.get(technoBuilder.build(), technoBuilder.getVersionType(), String.class));
            assertNotFound();
        });

        Then("^the techno deletion is rejected with a not found error$", () -> {
            assertNotFound();
        });

        Then("^the techno deletion is rejected with a conflict error$", () -> {
            assertConflict();
        });

        Then("^this techno templates are also deleted$", () -> {
            assertOK();
            testContext.setResponseEntity(technoClient.getTemplates(technoBuilder.build(), PartialTemplateIO[].class));
            assertEquals(0, getBodyAsArray().length);
            testContext.setResponseEntity(technoClient.getTemplate(technoBuilder.build().getName(), technoBuilder.build(), String.class));
            assertNotFound();
        });

    }
}
