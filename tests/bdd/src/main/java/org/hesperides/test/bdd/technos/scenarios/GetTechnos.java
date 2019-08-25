package org.hesperides.test.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.technos.TechnoBuilder;
import org.hesperides.test.bdd.technos.TechnoClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;

public class GetTechnos extends HesperidesScenario implements En {

    @Autowired
    private TechnoClient technoClient;
    @Autowired
    private TechnoBuilder technoBuilder;

    public GetTechnos() {

        Given("^a techno that doesn't exist$", () -> technoBuilder.withName("doesn-t-exist"));

        When("^I( try to)? get the techno detail(?: for a techno type \"([^\"]*)\")?( with the wrong letter case)?$", (
                String tryTo, String technoType, String withWrongLetterCase) -> {

            if (isNotEmpty(technoType)) {
                technoBuilder.withVersionType(technoType);
            }
            if (isNotEmpty(withWrongLetterCase)) {
                technoBuilder.withName(technoBuilder.getName().toUpperCase());
            }
            technoClient.getTechno(technoBuilder.build(), technoBuilder.getVersionType(), tryTo);
        });

        When("^I get the technos name$", () -> technoClient.getTechnoNames());

        When("^I get the techno types$", () -> {
            TechnoIO techno = technoBuilder.build();
            technoClient.getTechnoTypes(techno.getName(), techno.getVersion());
        });

        When("^I get the techno versions$", () -> technoClient.getTechnoVersions("new-techno"));

        Then("^the techno detail is successfully retrieved$", () -> {
            assertOK();
            TechnoIO expectedTechno = technoBuilder.build();
            TechnoIO actualTechno = testContext.getResponseBody();
            assertEquals(expectedTechno, actualTechno);
        });
    }
}
