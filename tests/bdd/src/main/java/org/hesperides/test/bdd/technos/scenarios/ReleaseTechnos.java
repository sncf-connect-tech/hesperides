package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.hesperides.test.bdd.templatecontainers.TemplateContainerHelper;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ReleaseTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;

    public ReleaseTechnos() {

        Given("^a released techno( with properties)?$", (String withProperties) -> {
            if (StringUtils.isNotEmpty(withProperties)) {
                templateBuilder.withContent("foo").withContent("bar");
            }
            technoClient.create(templateBuilder.build(), technoBuilder.build());
            technoClient.release(technoBuilder.build());
            technoBuilder.withVersionType(TemplateContainerHelper.RELEASE);
        });

        When("^I( try to)? release this techno$", (String tryTo) -> {
            testContext.responseEntity = technoClient.release(technoBuilder.build(), getResponseType(tryTo, TechnoIO.class));
        });

        Then("^the techno is successfully released$", () -> {
            assertCreated();
            TechnoBuilder expectedTechnoBuilder = new TechnoBuilder().withVersionType(TemplateContainerHelper.RELEASE);
            TechnoIO expectedTechno = expectedTechnoBuilder.build();
            TechnoIO actualTechno = (TechnoIO) testContext.getResponseBody();
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
            assertNotFound();
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });

        Then("^the techno release is rejected with a conflict error$", () -> {
            assertConflict();
        });
    }
}
