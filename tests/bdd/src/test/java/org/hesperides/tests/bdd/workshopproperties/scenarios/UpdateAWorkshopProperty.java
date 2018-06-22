package org.hesperides.tests.bdd.workshopproperties.scenarios;

import cucumber.api.java8.En;
import org.hesperides.presentation.io.WorkshopPropertyInput;
import org.hesperides.presentation.io.WorkshopPropertyOutput;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.workshopproperties.WorkshopPropertyAssertions;
import org.hesperides.tests.bdd.workshopproperties.contexts.WorkshopPropertyContext;
import org.hesperides.tests.bdd.workshopproperties.samples.WorkshopPropertySamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

public class UpdateAWorkshopProperty extends CucumberSpringBean implements En {

    @Autowired
    private WorkshopPropertyContext workshopPropertyContext;

    private ResponseEntity<WorkshopPropertyOutput> response;

    public UpdateAWorkshopProperty() {

        When("^updating this workshop property$", () -> {
            WorkshopPropertyInput workshopPropertyInput = WorkshopPropertySamples.getWorkshopPropertyInputWithValue("foo");
            response = workshopPropertyContext.updateWorkshopProperty(workshopPropertyInput);
        });

        Then("^the workshop property is successfully updated", () -> {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            WorkshopPropertyOutput actualOutput = response.getBody();
            WorkshopPropertyOutput expectedOutput = WorkshopPropertySamples.getWorkshopPropertyOutputWithValue("foo");
            WorkshopPropertyAssertions.assertWorkshopProperty(expectedOutput, actualOutput);
        });
    }
}
