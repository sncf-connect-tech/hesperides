package org.hesperides.tests.bdd.events;


import org.hamcrest.Matchers;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.junit.Assert;

import java.util.List;

public class EventsAssertions {

    public static void assertEventsSize(final List<EventOutput> events, final Integer size) {
        Assert.assertThat(events, Matchers.hasSize(size));
    }

    public static void assertEventType(final EventOutput event, final String eventType) {
        Assert.assertThat(event, Matchers.hasProperty("type", Matchers.endsWith(eventType)));
    }

}
