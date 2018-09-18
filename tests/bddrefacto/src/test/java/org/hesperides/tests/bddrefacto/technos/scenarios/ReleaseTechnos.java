package org.hesperides.tests.bddrefacto.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.hesperides.tests.bddrefacto.technos.TechnoBuilder;
import org.hesperides.tests.bddrefacto.technos.TechnoClient;
import org.hesperides.tests.bddrefacto.templatecontainers.TemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hesperides.tests.bddrefacto.technos.TechnoAssertions.assertTechno;
import static org.junit.Assert.assertEquals;

public class ReleaseTechnos implements En {

    @Autowired
    private TechnoBuilder technoBuilder;
    @Autowired
    private TemplateBuilder templateBuilder;
    @Autowired
    private TechnoClient technoClient;

    private ResponseEntity responseEntity;

    public ReleaseTechnos() {

        Given("^a released techno( with properties)?$", (String withProperties) -> {
            technoBuilder = new TechnoBuilder();
            templateBuilder = new TemplateBuilder();

            if (StringUtils.isNotEmpty(withProperties)) {
                templateBuilder.withProperty("foo").withProperty("bar");
            }

            technoClient.create(templateBuilder.build(), technoBuilder.build(), TemplateIO.class);
            technoClient.releaseTechno(technoBuilder.getTechnoKey(), TechnoIO.class);
        });

        When("^I release this techno$", () -> {
            responseEntity = technoClient.releaseTechno(technoBuilder.getTechnoKey(), TechnoIO.class);
        });

        When("^I try to release this techno$", () -> {
            responseEntity = technoClient.releaseTechno(technoBuilder.getTechnoKey(), String.class);
        });

        Then("^the techno is successfully released$", () -> {
            assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
            TechnoIO expectedTechno = technoBuilder.withIsWorkingCopy(false).build();
            TechnoIO actualTechno = (TechnoIO) responseEntity.getBody();
            assertTechno(expectedTechno, actualTechno);
        });

        Then("^the techno release is rejected with a not found error$", () -> {
            assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
            //TODO Vérifier si on doit renvoyer le même message que dans le legacy et tester le cas échéant
        });
    }
}
