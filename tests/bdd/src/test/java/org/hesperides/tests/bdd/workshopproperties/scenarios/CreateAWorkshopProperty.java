package org.hesperides.tests.bdd.workshopproperties.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.WorkshopPropertyInput;
import org.hesperides.core.presentation.io.WorkshopPropertyOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.workshopproperties.WorkshopPropertyAssertions;
import org.hesperides.tests.bdd.workshopproperties.contexts.WorkshopPropertyContext;
import org.hesperides.tests.bdd.workshopproperties.samples.WorkshopPropertySamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class CreateAWorkshopProperty extends CucumberSpringBean implements En {

    @Autowired
    private WorkshopPropertyContext workshopPropertyContext;

    private WorkshopPropertyInput input;
    private ResponseEntity<WorkshopPropertyOutput> response;

    public CreateAWorkshopProperty() {
        Given("a workshop property to create$", () -> {
            input = WorkshopPropertySamples.getWorkshopPropertyInputWithDefaultValues();
        });

        When("^creating this workshop property$", () -> {
            response = workshopPropertyContext.createWorkshopProperty(input);
        });

        Then("^the workshop property is successfully created$", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            WorkshopPropertyOutput actualOutput = response.getBody();
            WorkshopPropertyOutput expectedOutput = WorkshopPropertySamples.getWorkshopPropertyOutputWithDefaultValues();
            WorkshopPropertyAssertions.assertWorkshopProperty(expectedOutput, actualOutput);
        });
    }
}
