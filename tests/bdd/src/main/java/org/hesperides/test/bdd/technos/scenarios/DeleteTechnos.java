package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;
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
            technoClient.deleteTechno(technoBuilder.build(), tryTo);
            if (isEmpty(tryTo)) {
                technoHistory.removeTechnoBuilder(technoBuilder);
            }
        });

        Then("^the techno is successfully deleted$", () -> {
            assertOK();
            technoClient.getTechno(technoBuilder.build(), "should-fail");
            assertNotFound();
        });

        Then("^the techno deletion is rejected with a not found error$", this::assertNotFound);

        Then("^the techno deletion is rejected with a conflict error$", this::assertConflict);

        Then("^this techno templates are also deleted$", () -> {
            assertOK();
            technoClient.getTemplates(technoBuilder.build());
            assertEquals(0, testContext.getResponseBodyArrayLength());
            technoBuilder.getTemplateBuilders().forEach(templateBuilder -> {
                technoClient.getTemplate(templateBuilder.getName(), technoBuilder.build(), "should-fail");
                assertNotFound();
            });
        });

    }
}
