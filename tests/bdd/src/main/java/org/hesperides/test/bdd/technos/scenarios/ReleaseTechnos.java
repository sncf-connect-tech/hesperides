package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ReleaseTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoHistory technoHistory;

    public ReleaseTechnos() {

        When("^I( try to)? release this techno$", (String tryTo) -> {
            technoClient.release(technoBuilder.build(), getResponseType(tryTo, TechnoIO.class));
                        technoBuilder.withVersionType(VersionType.RELEASE);
            technoBuilder.updateTemplatesNamespace();
            technoHistory.addTechnoBuilder(technoBuilder);
        });

        Then("^the techno release is rejected with a not found error$", this::assertNotFound);

        Then("^the techno release is rejected with a conflict error$", this::assertConflict);
    }
}
