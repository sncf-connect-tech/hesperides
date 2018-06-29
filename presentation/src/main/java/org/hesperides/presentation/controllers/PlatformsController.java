package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hesperides.application.platforms.PlatformUseCases;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.queries.views.ApplicationSearchView;
import org.hesperides.domain.platforms.queries.views.ApplicationView;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.presentation.io.platforms.ApplicationSearchOutput;
import org.hesperides.presentation.io.platforms.ApplicationOutput;
import org.hesperides.presentation.io.platforms.PlatformIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.domain.security.User.fromAuthentication;

@Api("/applications")
@RequestMapping("/applications")
@RestController
public class PlatformsController extends AbstractController {

    private final PlatformUseCases platformUseCases;

    @Autowired
    public PlatformsController(PlatformUseCases platformUseCases) {
        this.platformUseCases = platformUseCases;
    }

    @GetMapping("/{application_name}")
    @ApiOperation("Get applications")
    public ResponseEntity<ApplicationOutput> getApplications(@PathVariable("application_name") final String applicationName) {
        ApplicationView applicationView = platformUseCases.getApplication(applicationName);
        ApplicationOutput applicationOutput = new ApplicationOutput(applicationView);

        return ResponseEntity.ok(applicationOutput);
    }

    @PostMapping("/{application_name}/platforms")
    @ApiOperation("Create platform")
    public ResponseEntity<PlatformIO> createPlatform(Authentication authentication,
                                                     @PathVariable("application_name") final String applicationName,
                                                     @Valid @RequestBody final PlatformIO platformInput) {

        Platform platform = platformInput.toDomainInstance();
        Platform.Key createdPlatformKey = platformUseCases.createPlatform(platform, fromAuthentication(authentication));


        PlatformView platformView = platformUseCases.getPlatform(createdPlatformKey);
        PlatformIO platformOutput = new PlatformIO(platformView);

        return ResponseEntity.ok(platformOutput);
    }

    @ApiOperation("Retrieve a platform")
    @GetMapping("/{application_name}/platforms/{platform_name}")
    public ResponseEntity<PlatformIO> getPlatform(@PathVariable("application_name") final String applicationName,
                                                  @PathVariable("platform_name") final String platformName) {

        // create key from path
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);

        // retrieve platform
        PlatformView platformView = platformUseCases.getPlatform(platformKey);

        // transform it into IO
        PlatformIO platformOutput = new PlatformIO(platformView);

        // response
        return ResponseEntity.ok(platformOutput);
    }

    @ApiOperation("Update a platform")
    @PutMapping("/{application_name}/platforms")
    public ResponseEntity<PlatformIO> updatePlatform(Authentication authentication,
                                                     @PathVariable("application_name") final String applicationName,
                                                     @RequestParam(value = "copyPropertiesForUpgradedModules", required = false) final Boolean copyProps,
                                                     @Valid @RequestBody final PlatformIO newDefinition) {

        final boolean copyRequested = Boolean.TRUE.equals(copyProps); // no null anymore
        // create key from path
        Platform.Key platformKey = new Platform.Key(applicationName, newDefinition.getPlatformName());

        // perform update
        platformUseCases.updatePlatform(platformKey,
                newDefinition.toDomainInstance(),
                copyRequested,
                fromAuthentication(authentication)
        );

        // retrieve updated view
        PlatformView platformView = platformUseCases.getPlatform(platformKey);

        // response
        final ResponseEntity.BodyBuilder response = ResponseEntity.status(200);
        if (copyRequested) {
            // TODO remove as soon as properties are handled
            response.header("x-hesperides-warning", "no property copied! (not implemented yet)");
        }
        return response.body(new PlatformIO(platformView));
    }

    @ApiOperation("Delete a platform")
    @DeleteMapping("/{application_name}/platforms/{platform_name}")
    public ResponseEntity deletePlatform(Authentication authentication,
                                         @PathVariable("application_name") final String applicationName,
                                         @PathVariable("platform_name") final String platformName) {

        // create key from path
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);

        // delete platform with this key
        platformUseCases.deletePlatform(platformKey, fromAuthentication(authentication));

        // response
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Search an application")
    @PostMapping("/perform_search")
    public ResponseEntity searchApplication(Authentication authentication,
                                            @RequestParam("name") final String input) {

        // search applications
        List<ApplicationSearchView> applicationsView = platformUseCases.searchApplications(input);

        // transform it into IO
        List<ApplicationSearchOutput> applicationSearchOutput = applicationsView != null
                ? applicationsView.stream()
                .distinct()
                .map(ApplicationSearchOutput::new).collect(Collectors.toList())
                : new ArrayList<>();

        // response
        return ResponseEntity.ok(applicationSearchOutput);
    }
}
