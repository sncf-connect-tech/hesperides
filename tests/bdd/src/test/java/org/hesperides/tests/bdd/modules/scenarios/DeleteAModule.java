package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteAModule extends CucumberSpringBean implements En {

    @Autowired
    private ExistingModuleContext existingModuleContext;

    public DeleteAModule() {
        When("^deleting this module$", () -> {
            rest.delete(existingModuleContext.getModuleLocation());
        });

        Then("^the module is successfully deleted$", () -> {
            ResponseEntity<String> entity = rest.doWithErrorHandlerDisabled(template1 ->
                    template1.getForEntity(existingModuleContext.getModuleLocation(), String.class));
            assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }
}
