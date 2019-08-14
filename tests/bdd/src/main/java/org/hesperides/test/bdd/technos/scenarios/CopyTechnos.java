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
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CopyTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoHistory technoHistory;

    public CopyTechnos() {

        When("^I( try to)? create a copy of this techno(, using the same key)?$", (String tryTo, String sameKey) -> {
            TechnoIO existingTechno = technoBuilder.build();
            if (StringUtils.isEmpty(sameKey)) {
                technoBuilder.withVersion("1.1");
            }
            technoClient.copy(existingTechno, technoBuilder.build(), getResponseType(tryTo, TechnoIO.class));
            // Dans le cas d'une copie de release, la techno
            // créée devient automatiquement une working copy
            technoBuilder.withVersionType(VersionType.WORKINGCOPY);
            // Les templates sont identiques sauf pour le namespace
            technoBuilder.updateTemplatesNamespace();
            technoHistory.addTechnoBuilder(technoBuilder);
        });

        Then("^the techno is successfully (?:duplicated|released)$", () -> {
            assertCreated();
            TechnoIO expectedTechno = technoBuilder.build();
            TechnoIO actualTechno = testContext.getResponseBody(TechnoIO.class);
            assertEquals(expectedTechno, actualTechno);

            List<PartialTemplateIO> expectedTemplates = technoBuilder.getTemplateBuilders()
                    .stream()
                    .map(TemplateBuilder::buildPartialTemplate)
                    .collect(Collectors.toList());

            technoClient.getTemplates(actualTechno, PartialTemplateIO[].class);
            List<PartialTemplateIO> actualTemplates = testContext.getResponseBodyAsList();
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the model of the techno is the same$", () -> {
            technoClient.getModel(technoBuilder.build(), ModelOutput.class);
            assertOK();
            ModelOutput expectedModel = technoHistory.getFirstTechnoBuilder().buildPropertiesModel();
            ModelOutput actualModel = testContext.getResponseBody(ModelOutput.class);
            assertEquals(expectedModel, actualModel);
        });

        Then("^the techno copy is rejected with a not found error$", this::assertNotFound);

        Then("^the techno copy is rejected with a conflict error$", this::assertConflict);
    }
}
