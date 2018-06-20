package org.hesperides.tests.bdd.technos.scenarios;

import cucumber.api.java8.En;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.technos.TechnoAssertions;
import org.hesperides.tests.bdd.technos.TechnosSamples;
import org.hesperides.tests.bdd.technos.contexts.TechnoContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CopyATechno extends CucumberSpringBean implements En {

    @Autowired
    private TechnoContext technoContext;

    private ResponseEntity<TechnoIO> response;

    public CopyATechno() {

        When("^creating a copy of this techno$", () -> {
            TechnoIO technoInput = TechnosSamples.getTechnoWithNameAndVersion("techno-copy", "1.0.0");
            TemplateContainer.Key technoKey = technoContext.getTechnoKey();
            response = rest.getTestRest().postForEntity("/templates/packages?from_package_name={moduleName}&from_package_version={moduleVersion}&from_is_working_copy={isWorkingCopy}",
                    technoInput, TechnoIO.class,
                    technoKey.getName(), technoKey.getVersion(), technoKey.isWorkingCopy());
        });

        Then("^the techno is successfully and completely duplicated$", () -> {
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            TechnoIO actualTechoOutput = response.getBody();
            TechnoIO expectedTechnoOutput = TechnosSamples.getTechnoWithNameAndVersion("techno-copy", "1.0.0");
            TechnoAssertions.assertTechno(expectedTechnoOutput, actualTechoOutput);
            //TODO Assert templates
        });

        Then("^the model of the techno is also duplicated$", () -> {
            ResponseEntity<ModelOutput> modelResponse = rest.getTestRest().getForEntity(technoContext.getTechnoURI(
                    new Techno.Key("techno-copy", "1.0.0", TemplateContainer.VersionType.workingcopy)) + "/model", ModelOutput.class);
            assertEquals(HttpStatus.OK, modelResponse.getStatusCode());
            ModelOutput modelOutput = modelResponse.getBody();
            assertEquals(1, modelOutput.getProperties().size());
        });
    }
}
