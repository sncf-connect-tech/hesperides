package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.apache.commons.lang3.StringUtils.*;
import static org.junit.Assert.assertEquals;

public class GetTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;

    public GetTechnos() {

        Given("^a techno that doesn't exist$", () -> technoBuilder.withName("doesn-t-exist"));

        When("^I( try to)? get the techno detail(?: for a techno type \"(.*)\")?( with the wrong letter case)?$", (String tryTo, String technoType, String withWrongLetterCase) -> {
            if (isNotEmpty(technoType)) {
                technoBuilder.withVersionType(technoType);
            }
            TechnoIO technoInput = technoBuilder.build();
            if (isNotEmpty(withWrongLetterCase)) {
                technoInput = new TechnoBuilder().withName(technoBuilder.getName().toUpperCase()).build();
            }
            ResponseEntity responseEntity = technoClient.get(technoInput, technoBuilder.getVersionType(), getResponseType(tryTo, TechnoIO.class));
            testContext.setResponseEntity(responseEntity);
        });

        Then("^the techno detail is successfully retrieved$", () -> {
            assertOK();
            TechnoIO expectedTechno = technoBuilder.build();
            TechnoIO actualTechno = testContext.getResponseBody(TechnoIO.class);
            assertEquals(expectedTechno, actualTechno);
        });
    }
}
