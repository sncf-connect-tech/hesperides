package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
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
            ResponseEntity responseEntity = technoClient.release(technoBuilder.build(), getResponseType(tryTo, TechnoIO.class));
            testContext.setResponseEntity(responseEntity);
            technoBuilder.withIsWorkingCopy(false);
            technoHistory.addTechnoBuilder(technoBuilder);
        });

        Then("^the techno is successfully released$", () -> {
            assertCreated();
            TechnoIO expectedTechno = technoBuilder.build();
            TechnoIO actualTechno = testContext.getResponseBody(TechnoIO.class);
            assertEquals(expectedTechno, actualTechno);

            // Compare les templates de la techno d'origine avec ceux de la techno en mode release. Seul le namespace est différent.
            TechnoBuilder originTechno = technoHistory.findTechnoBuilder(expectedTechno.getName(), expectedTechno.getVersion(), true);
            List<PartialTemplateIO> expectedTemplates = originTechno.getTemplateBuilders()
                    .stream()
                    .map(TemplateBuilder::buildPartialTemplate)
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualTemplates = technoClient.getTemplates(actualTechno);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the techno release is rejected with a not found error$", this::assertNotFound);

        Then("^the techno release is rejected with a conflict error$", this::assertConflict);
    }
}
