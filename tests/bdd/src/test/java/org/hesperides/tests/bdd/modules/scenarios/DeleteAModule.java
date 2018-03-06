package org.hesperides.tests.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.modules.contexts.ExistingModuleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteAModule extends CucumberSpringBean implements En {

    @Autowired
    ExistingModuleContext moduleContext;

    public DeleteAModule() {
        When("^deleting this module$", () -> {
            template.delete(moduleContext.getModuleLocation());
        });

        Then("^the module is successfully deleted$", () -> {
            ResponseEntity<String> responseEntity = template.doWithErrorHandlerDisabled(template1 -> template1.getForEntity(moduleContext.getModuleLocation(), String.class));
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(404);
        });
    }
}
