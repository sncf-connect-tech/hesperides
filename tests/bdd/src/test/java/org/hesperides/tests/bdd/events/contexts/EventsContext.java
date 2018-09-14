package org.hesperides.tests.bdd.events.contexts;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.tests.bdd.commons.tools.HesperideTestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hesperides.core.domain.templatecontainers.entities.TemplateContainer.VersionType.release;
import static org.hesperides.core.domain.templatecontainers.entities.TemplateContainer.VersionType.workingcopy;

@Component
public class EventsContext {

    @Autowired
    private HesperideTestRestTemplate rest;

    public EventsContext() {
    }

    public List<EventOutput> getEvents(ModuleIO moduleInput) {
        ResponseEntity<EventOutput[]> response = rest.getTestRest()
                .getForEntity("/events/modules/{name}/{version}/{version_type}", EventOutput[].class, moduleInput.getName(), moduleInput.getVersion(), moduleInput.isWorkingCopy() ? workingcopy.name() : release.name());
        return Arrays.asList(response.getBody());
    }
}
