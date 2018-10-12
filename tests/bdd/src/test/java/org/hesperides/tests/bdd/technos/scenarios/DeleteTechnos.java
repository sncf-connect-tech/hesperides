package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bdd.commons.StepHelper.*;

public class DeleteTechnos implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    private ResponseEntity responseEntity;

    public DeleteTechnos() {

        When("^I( try to)? delete this techno$", (String tryTo) -> {
            responseEntity = technoClient.delete(technoBuilder.build(), getResponseType(tryTo, ResponseEntity.class));
            moduleBuilder.removeTechno(technoBuilder.build());
            modelBuilder.removeProperties(technoBuilder.getProperties());
        });

        Then("^the techno is successfully deleted$", () -> {
            assertOK(responseEntity);
            responseEntity = technoClient.get(technoBuilder.build(), String.class);
            assertNotFound(responseEntity);
        });

        Then("^the techno deletion is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
        });
    }
}
