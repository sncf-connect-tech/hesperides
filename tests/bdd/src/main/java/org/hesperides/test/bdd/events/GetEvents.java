package org.hesperides.test.bdd.events;

import com.google.gson.internal.LinkedTreeMap;
import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.test.bdd.commons.HesperidesScenario;
import org.hesperides.test.bdd.modules.ModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class GetEvents extends HesperidesScenario implements En {

    @Autowired
    private EventClient eventClient;
    @Autowired
    private ModuleBuilder moduleBuilder;
    @Autowired
    private PlatformBuilder platformBuilder;

    public GetEvents() {

        When("^I( try to)? get the events of this module$", (String tryTo) -> {
            eventClient.getModuleEvents(moduleBuilder.build(), tryTo);
        });

        When("^I( try to)? get the events of this platform$", (String tryTo) -> {
            eventClient.getPlatformEvents(platformBuilder.buildInput(), tryTo);
        });

        Then("^(\\d+) event(?: is|s are) returned$", (Integer nbEvents) -> {
            assertOK();
            List<EventOutput> events = testContext.getResponseBodyAsList();
            assertThat(events, hasSize(nbEvents));
        });

        Then("^event at index (\\d+) is a (.*) event type$", (Integer index, String eventType) -> {
            List<EventOutput> events = testContext.getResponseBodyAsList();
            assertThat(events.get(index), hasProperty("type", endsWith(eventType)));
        });

        Then("^one of the events should contain the comment \"([^\"]*)\"", (String comment) -> {
            List<EventOutput> events = testContext.getResponseBodyAsList();
            boolean commentFound = false;
            for (EventOutput event : events) {
                LinkedTreeMap data = (LinkedTreeMap) event.getData();
                if (data.containsKey("userComment") && data.get("userComment").toString().equals(comment)) {
                    commentFound = true;
                    break;
                }
            }
            assertTrue("The comment was not found", commentFound);
        });
    }
}
