package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
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
            testContext.responseEntity = technoClient.delete(technoBuilder.build(), getResponseType(tryTo, ResponseEntity.class));
            moduleBuilder.removeTechno(technoBuilder.build());
            modelBuilder.removeProperties(technoBuilder.getProperties());
        });

        Then("^the techno is successfully deleted$", () -> {
            assertOK();
            testContext.responseEntity = technoClient.get(technoBuilder.build(), technoBuilder.getVersionType(), String.class);
            assertNotFound();
        });

        Then("^the techno deletion is rejected with a not found error$", () -> {
            assertNotFound();
        });

        Then("^this techno templates are also deleted$", () -> {
            assertOK();
            testContext.responseEntity = technoClient.getTemplates(technoBuilder.build(), PartialTemplateIO[].class);
            assertEquals(0, getBodyAsArray().length);
            testContext.responseEntity = technoClient.getTemplate(technoBuilder.build().getName(), technoBuilder.build(), String.class);
            assertNotFound();
        });

    }
}
