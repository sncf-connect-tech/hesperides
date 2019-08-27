package org.hesperides.test.bdd.modules.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.modules.ModuleClient;
import org.hesperides.test.bdd.modules.ModuleHistory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DeleteModules extends HesperidesScenario implements En {

    @Autowired
    private ModuleClient moduleClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private ModuleHistory moduleHistory;

    public DeleteModules() {

        When("^I( try to)? delete this module$", (String tryTo) -> {
            moduleClient.deleteModule(moduleBuilder.build(), tryTo);
            if (isEmpty(tryTo)) {
                moduleHistory.removeModuleBuilder(moduleBuilder);
                moduleBuilder.withVersionId(0);
            }
        });

        Then("^the module is successfully deleted$", () -> {
            assertOK();
            moduleClient.getModule(moduleBuilder.build(), "should-fail");
            assertNotFound();
        });

        Then("^the module deletion is rejected with a not found error$", this::assertNotFound);

        Then("^the module deletion is rejected with a conflict error$", this::assertConflict);

        Then("^this module templates are also deleted$", () -> {
            assertOK();
            assertThat(moduleClient.getTemplates(moduleBuilder.build()), hasSize(0));
            moduleBuilder.getTemplateBuilders().forEach(templateBuilder -> {
                moduleClient.getTemplate(templateBuilder.getName(), moduleBuilder.build(), "should-fail");
                assertNotFound();
            });
        });

    }
}
