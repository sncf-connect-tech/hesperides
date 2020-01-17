package org.hesperides.test.bdd.platforms.scenarios;


import java.util.List;

import static org.junit.Assert.assertEquals;
import org.hesperides.core.presentation.io.platforms.properties.events.PropertiesEventOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.platforms.PlatformClient;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.DataTable;
import cucumber.api.java8.En;

public class GetPropertiesEvents extends HesperidesScenario implements En {
    private static final String APPLICATION_NAME = "test-application";
    private static final String PLATEFORME_NAME = "P1";
    private static final String PROPERTIES_PATH = "#ABC#DEF#test-module#1.0#WORKINGCOPY";


    @Autowired
    private PlatformClient platformClient;

    public GetPropertiesEvents() {

        When("^I get (last|previous)? module properties events", (String lastOrPrevious) -> {
            switch(lastOrPrevious) {
                case "last":
                    platformClient.getModulePropertiesEvents(APPLICATION_NAME, PLATEFORME_NAME,
                            PROPERTIES_PATH, null, null);
                    break;
                case "previous":
                    platformClient.getModulePropertiesEvents(APPLICATION_NAME, PLATEFORME_NAME,
                            PROPERTIES_PATH, 2, 1);
                    break;
                default:
                    throw new RuntimeException("You must choose between last or previous module properties events");
            }
        });

        When("^I try to get module properties events with an invalid (property path|platform version type)?", (String invalidSearch) -> {
            switch(invalidSearch) {
                case "property path":
                    platformClient.getModulePropertiesEvents(APPLICATION_NAME, PLATEFORME_NAME,
                            PROPERTIES_PATH, null, null);
                    break;
                case "platform version type":
                    platformClient.getModulePropertiesEvents(APPLICATION_NAME, PLATEFORME_NAME,
                            PROPERTIES_PATH, null, null);
                    break;
                default:
                    throw new RuntimeException("You must choose between last or previous module properties events");
            }
        });

        Then("the resulting properties events matches", (DataTable data) -> {
            PropertiesEventOutput[] actualEvents = testContext.getResponseBody(PropertiesEventOutput[].class);
            List<PropertiesEventOutput> expectedEvents = data.asList(PropertiesEventOutput.class);

            assertEquals(actualEvents.length, expectedEvents.size());
            // TODO c'est dégueu
            for (int i=0 ; i<actualEvents.length ; i++) {
                PropertiesEventOutput actualEvent = actualEvents[i];
                PropertiesEventOutput expectedEvent = expectedEvents.get(i);

                // Check all datas
                assertEquals(actualEvent.getAuthor(), expectedEvent.getAuthor());
                assertEquals(actualEvent.getComment(), expectedEvent.getComment());
//                assertEquals(actualEvent.getRemovedProperties(), expectedEvent.getAddedProperties());
//                assertEquals(actualEvent.getAddedProperties(), expectedEvent.getAddedProperties());
            }

            assertOK();
        });

        Then("there is no events founded", () -> {
            PropertiesEventOutput[] actualEvents = testContext.getResponseBody(PropertiesEventOutput[].class);
            assertEquals(actualEvents.length, 0);
        });
    }
}
