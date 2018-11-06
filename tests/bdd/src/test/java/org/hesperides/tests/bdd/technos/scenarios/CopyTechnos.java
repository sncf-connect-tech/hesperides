package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.tests.bdd.commons.HesperidesScenario;
import org.hesperides.tests.bdd.technos.TechnoBuilder;
import org.hesperides.tests.bdd.technos.TechnoClient;
import org.hesperides.tests.bdd.templatecontainers.builders.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.tests.bdd.commons.HesperidesScenario.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private ModelBuilder modelBuilder;

    public CopyTechnos() {

        When("^I( try to)? create a copy of this techno$", (String tryTo) -> {
            testContext.responseEntity = copy("1.0.1", getResponseType(tryTo, TechnoIO.class));
        });

        When("^I try to create a copy of this techno, using the same key$", () -> {
            testContext.responseEntity = copy(technoBuilder.build().getVersion(), String.class);
        });

        Then("^the techno is successfully duplicated$", () -> {
            assertCreated();
            TechnoBuilder expectedTechnoBuilder = new TechnoBuilder().withVersion("1.0.1");
            TechnoIO expectedTechno = expectedTechnoBuilder.build();
            TechnoIO actualTechno = (TechnoIO) testContext.getResponseBody();
            assertEquals(expectedTechno, actualTechno);

            // Vérifie la liste des templates
            // Seul le namespace est différent
            String expectedNamespace = expectedTechnoBuilder.getNamespace();
            List<PartialTemplateIO> expectedTemplates = technoClient.getTemplates(technoBuilder.build())
                    .stream()
                    .map(expectedTemplate -> new PartialTemplateIO(expectedTemplate.getName(), expectedNamespace, expectedTemplate.getFilename(), expectedTemplate.getLocation()))
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualTemplates = technoClient.getTemplates(actualTechno);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the model of the techno is the same$", () -> {
            testContext.responseEntity = technoClient.getModel(technoBuilder.build(), ModelOutput.class);
            assertOK();
            ModelOutput expectedModel = modelBuilder.build();
            ModelOutput actualModel = (ModelOutput) testContext.getResponseBody();
            assertEquals(expectedModel, actualModel);
        });

        Then("^the version type of the duplicated techno is working copy$", () -> {
            TechnoIO techoOutput = (TechnoIO) testContext.getResponseBody();
            assertTrue(techoOutput.isWorkingCopy());
        });

        Then("^the techno copy is rejected with a not found error$", () -> {
            assertNotFound();
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });

        Then("^the techno copy is rejected with a conflict error$", () -> {
            assertConflict();
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }

    private ResponseEntity copy(String newVersion, Class responseType) {
        TechnoIO newTechnoInput = new TechnoBuilder().withVersion(newVersion).build();
        return technoClient.copy(technoBuilder.build(), newTechnoInput, responseType);
    }
}
