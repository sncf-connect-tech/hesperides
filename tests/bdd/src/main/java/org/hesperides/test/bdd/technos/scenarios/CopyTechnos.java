package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.technos.TechnoHistory;
import org.hesperides.test.bdd.templatecontainers.builders.ModelBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoHistory technoHistory;
    @Autowired
    private ModelBuilder modelBuilder;

    public CopyTechnos() {

        When("^I( try to)? create a copy of this techno(, using the same key)?$", (String tryTo, String sameKey) -> {
            TechnoIO existingTechno = technoBuilder.build();
            if (StringUtils.isEmpty(sameKey)) {
                technoBuilder.withVersion("1.1");
            }
            ResponseEntity responseEntity = technoClient.copy(existingTechno, technoBuilder.build(), getResponseType(tryTo, TechnoIO.class));
            testContext.setResponseEntity(responseEntity);
            technoHistory.addTechnoBuilder(technoBuilder);
        });

        Then("^the techno is successfully duplicated$", () -> {
            assertCreated();
            TechnoIO expectedTechno = technoBuilder.build();
            TechnoIO actualTechno = testContext.getResponseBody(TechnoIO.class);
            assertEquals(expectedTechno, actualTechno);

            // Compare les templates de la techno d'origine avec ceux de la techno copiée. Seul le namespace est différent.
            List<PartialTemplateIO> expectedTemplates = technoHistory.getFirstTechnoBuilder().getTemplateBuilders()
                    .stream()
                    .map(TemplateBuilder::buildPartialTemplate)
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualTemplates = technoClient.getTemplates(actualTechno);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the model of the techno is the same$", () -> {
            testContext.setResponseEntity(technoClient.getModel(technoBuilder.build(), ModelOutput.class));
            assertOK();
            ModelOutput expectedModel = technoHistory.getFirstTechnoBuilder().getPropertiesModel();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the version type of the duplicated techno is working copy$", () -> {
            TechnoIO technoOutput = testContext.getResponseBody(TechnoIO.class);
            assertTrue(technoOutput.getIsWorkingCopy());
        });

        Then("^the techno copy is rejected with a not found error$", this::assertNotFound);

        Then("^the techno copy is rejected with a conflict error$", this::assertConflict);
    }

    private ResponseEntity copy(String newVersion, Class responseType) {
        TechnoIO newTechnoInput = new TechnoBuilder().withVersion(newVersion).build();
        return technoClient.copy(technoBuilder.build(), newTechnoInput, responseType);
    }
}
