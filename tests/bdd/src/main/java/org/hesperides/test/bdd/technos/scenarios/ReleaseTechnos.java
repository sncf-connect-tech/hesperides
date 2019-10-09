package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
import org.hesperides.test.bdd.templatecontainers.TestVersionType;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ReleaseTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoHistory technoHistory;

    public ReleaseTechnos() {

        When("^I( try to)? release this techno$", (String tryTo) -> {
            release(tryTo);
        });

        Then("^the techno release is rejected with a not found error$", this::assertNotFound);

        Then("^the techno release is rejected with a conflict error$", this::assertConflict);
    }

    public void release(String tryTo) {
        technoClient.releaseTechno(technoBuilder.build(), tryTo);
        if (isEmpty(tryTo)) {
            technoBuilder.withVersionType(TestVersionType.RELEASE);
            technoBuilder.updateTemplatesNamespace();
            technoHistory.addTechnoBuilder(technoBuilder);
        }
    }
}
