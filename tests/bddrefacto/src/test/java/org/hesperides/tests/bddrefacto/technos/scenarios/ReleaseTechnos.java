package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.tests.bddrefacto.commons.StepHelper.*;
import static org.junit.Assert.assertEquals;

public class ReleaseTechnos implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    private ResponseEntity responseEntity;

    public ReleaseTechnos() {

        Given("^a released techno( with properties)?$", (String withProperties) -> {
            if (StringUtils.isNotEmpty(withProperties)) {
                templateBuilder.withContent("foo").withContent("bar");
            }
            technoClient.create(templateBuilder.build(), technoBuilder.build());
            technoClient.release(technoBuilder.build());
            technoBuilder.withIsWorkingCopy(false);
        });

        When("^I( try to)? release this techno$", (final String tryTo) -> {
            responseEntity = technoClient.release(technoBuilder.build(), getResponseType(tryTo, TechnoIO.class));
        });

        Then("^the techno is successfully released$", () -> {
            assertCreated(responseEntity);
            TechnoBuilder expectedTechnoBuilder = new TechnoBuilder().withIsWorkingCopy(false);
            TechnoIO expectedTechno = expectedTechnoBuilder.build();
            TechnoIO actualTechno = (TechnoIO) responseEntity.getBody();
            assertEquals(expectedTechno, actualTechno);

            // Compare les templates de la techno d'origine avec ceux de la techno en mode release
            // Seul le namespace est différent
            String expectedNamespace = expectedTechnoBuilder.getNamespace();
            List<PartialTemplateIO> expectedTemplates = technoClient.getTemplates(this.technoBuilder.build())
                    .stream()
                    .map(expectedTemplate -> new PartialTemplateIO(expectedTemplate.getName(), expectedNamespace, expectedTemplate.getFilename(), expectedTemplate.getLocation()))
                    .collect(Collectors.toList());
            List<PartialTemplateIO> actualTemplates = technoClient.getTemplates(actualTechno);
            assertEquals(expectedTemplates, actualTemplates);
        });

        Then("^the techno release is rejected with a not found error$", () -> {
            assertNotFound(responseEntity);
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }
}
