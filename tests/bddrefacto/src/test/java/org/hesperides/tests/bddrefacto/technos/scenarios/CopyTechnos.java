package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.tests.bddrefacto.commons.StepHelper;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyTechnos implements En {

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity responseEntity;

    public CopyTechnos() {

        When("^I( try to)? create a copy of this techno$", (final String tryTo) -> {
            responseEntity = copy("1.0.1", StepHelper.getResponseType(tryTo, TechnoIO.class));
        });

        When("^I try to create a copy of this techno, using the same key$", () -> {
            responseEntity = copy(technoBuilder.build().getVersion(), String.class);
        });

        Then("^the techno is successfully duplicated$", () -> {
            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            TechnoBuilder expectedTechnoBuilder = new TechnoBuilder().withVersion("1.0.1");
            TechnoIO expectedTechno = expectedTechnoBuilder.build();
            TechnoIO actualTechno = (TechnoIO) responseEntity.getBody();
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
            responseEntity = technoClient.getModel(technoBuilder.build(), ModelOutput.class);
            assertOK(responseEntity);
            ModelOutput modelOutput = (ModelOutput) responseEntity.getBody();
            assertEquals(2, modelOutput.getProperties().size());
            //TODO Créer un property builder et un model builder et faire l'assertion des propriétés
        });

        Then("^the version type of the duplicated techno is working copy$", () -> {
            TechnoIO techoOutput = (TechnoIO) responseEntity.getBody();
            assertTrue(techoOutput.isWorkingCopy());
        });

        Then("^the techno copy is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });

        Then("^the techno copy is rejected with a conflict error$", () -> {
            assertConflict(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }

    private ResponseEntity copy(final String newVersion, Class responseType) {
        TechnoIO newTechnoInput = new TechnoBuilder().withVersion(newVersion).build();
        return technoClient.copy(technoBuilder.build(), newTechnoInput, responseType);
    }
}
