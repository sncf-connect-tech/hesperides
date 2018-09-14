package org.hesperides.tests.bdd.events.scenarios;

import cucumber.api.java8.En;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.tests.bdd.events.EventsAssertions;
import org.hesperides.tests.bdd.events.contexts.EventsContext;
import org.hesperides.tests.bdd.modules.ModuleBuilder;
import org.hesperides.tests.bdd.modules.contexts.ModuleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class GetModuleEvents implements En {

    @Autowired
    EventsContext eventContext;
    @Autowired
    ModuleContext moduleContext;

    private List<EventOutput> events;

    public GetModuleEvents() {
        When("^get events occurred for the module created$", () -> {
            ModuleIO moduleInput = new ModuleBuilder().build();
            events = eventContext.getEvents(moduleInput);
        });

        Then("^(\\d+) event(?: is|s are) returned$", (Integer eventsSize) -> {
            EventsAssertions.assertEventsSize(events, eventsSize);
        });
        And("^event at index (\\d+) is a (.*) event type$", (Integer index, String eventType) -> {
            EventsAssertions.assertEventType(events.get(index), eventType);
        });
    }
}
