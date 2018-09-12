package org.hesperides.core.presentation.controllers;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.events.EventsUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Api("/events")
@RequestMapping("/events")
@RestController
public class EventsController extends AbstractController {

    EventsUseCases eventsUseCases;

    public EventsController(final EventsUseCases eventsUseCases) {
        this.eventsUseCases = eventsUseCases;
    }

    @ApiOperation("Get the events list from a stream name of platform or module")
    @GetMapping("/module/{module_name}/{module_version}/{module_type}")
    public ResponseEntity<List<EventOutput>> getEvents(@PathVariable("module_name") final String moduleName,
                                                       @PathVariable("module_version") final String moduleVersion,
                                                       @PathVariable("module_type") final String moduleType,
                                                       @RequestParam(value = "page", defaultValue = "1") final Integer page,
                                                       @RequestParam(value = "size", defaultValue = "25") final Integer size) {

        log.info("Get events from platform {}", moduleName);
        Module.Key key = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.valueOf(moduleType));
        List<EventOutput> events = eventsUseCases
                .getEvents(key)
                .stream()
                .map(EventOutput::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .body(events);
    }

}
