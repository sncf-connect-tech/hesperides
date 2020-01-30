package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.events.EventUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.events.EventOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Api(tags = "09. Events", description = " ")
@RequestMapping("/events")
@RestController
public class EventsController extends AbstractController {

    private final EventUseCases eventUseCases;

    @Autowired
    public EventsController(final EventUseCases eventUseCases) {
        this.eventUseCases = eventUseCases;
    }

    @Deprecated
    @ApiOperation("Get the events list from a legacy stream name of platform or module")
    @GetMapping("/{stream_name:.+}")
    public ResponseEntity<List<EventOutput>> getEvents(@PathVariable("stream_name") final String streamName,
                                                       @RequestParam(value = "page", defaultValue = "1") final Integer page,
                                                       @RequestParam(value = "size", defaultValue = "25") final Integer size) {
        List<EventOutput> events = eventUseCases
                .parseStreamNameAndGetEvents(streamName, page, size)
                .stream()
                .map(EventOutput::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2021-01-01\"")
                .header("Sunset", " Jan  15 00:00:00 CEST 2020")
                .header("Link", "/applications/{application_name}/platforms/{platform_name}/properties/events")
                .body(events);
    }

    @ApiOperation("Get the events list of a module")
    @GetMapping("/modules/{module_name}/{module_version}/{module_type}")
    public ResponseEntity<List<EventOutput>> getModuleEvents(@PathVariable("module_name") final String moduleName,
                                                             @PathVariable("module_version") final String moduleVersion,
                                                             @PathVariable("module_type") final String moduleType,
                                                             @RequestParam(value = "page", defaultValue = "1") final Integer page,
                                                             @RequestParam(value = "size", defaultValue = "25") final Integer size) {

        Module.Key key = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.valueOf(moduleType));
        log.info("Get events from module {}", key);
        List<EventOutput> events = eventUseCases
                .getEvents(key, page, size)
                .stream()
                .map(EventOutput::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(events);
    }

    @Deprecated
    @ApiOperation("Get the events list of a platform")
    @GetMapping("/platforms/{application_name}/{platform_name}")
    public ResponseEntity<List<EventOutput>> getPlatformEvents(@PathVariable("application_name") final String applicationName,
                                                               @PathVariable("platform_name") final String platformName,
                                                               @RequestParam(value = "page", defaultValue = "1") final Integer page,
                                                               @RequestParam(value = "size", defaultValue = "25") final Integer size) {

        Platform.Key key = new Platform.Key(applicationName, platformName);
        log.info("Get events from module {}", key);
        List<EventOutput> events = eventUseCases
                .getEvents(key, page, size)
                .stream()
                .map(EventOutput::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2021-01-01\"")
                .header("Sunset", " Jan  15 00:00:00 CEST 2020")
                .header("Link", String.format("/applications/%s/platforms/%s/events", applicationName, platformName))
                .body(events);
    }

}
