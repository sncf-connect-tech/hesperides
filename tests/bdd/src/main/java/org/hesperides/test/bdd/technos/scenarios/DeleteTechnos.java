package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
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
    private TechnoHistory technoHistory;

    public DeleteTechnos() {

        When("^I( try to)? delete this techno$", (String tryTo) -> {
            ResponseEntity responseEntity = technoClient.delete(technoBuilder.build(), getResponseType(tryTo, ResponseEntity.class));
            testContext.setResponseEntity(responseEntity);
            technoHistory.removeTechnoBuilder(technoBuilder);
        });

        Then("^the techno is successfully deleted$", () -> {
            assertOK();
            ResponseEntity responseEntity = technoClient.get(technoBuilder.build(), technoBuilder.getVersionType(), String.class);
            testContext.setResponseEntity(responseEntity);
            assertNotFound();
        });

        Then("^the techno deletion is rejected with a not found error$", this::assertNotFound);

        Then("^the techno deletion is rejected with a conflict error$", this::assertConflict);

        Then("^this techno templates are also deleted$", () -> {
            assertOK();
            ResponseEntity responseEntity = technoClient.getTemplates(technoBuilder.build(), PartialTemplateIO[].class);
            testContext.setResponseEntity(responseEntity);
            assertEquals(0, getBodyAsArray().length);
            technoBuilder.getTemplateBuilders().forEach(templateBuilder -> {
                ResponseEntity response = technoClient.getTemplate(templateBuilder.getName(), technoBuilder.build(), String.class);
                testContext.setResponseEntity(response);
                assertNotFound();
            });
        });

    }
}
