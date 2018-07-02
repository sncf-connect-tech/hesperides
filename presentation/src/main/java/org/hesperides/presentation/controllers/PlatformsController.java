package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.application.platforms.PlatformUseCases;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.queries.views.ApplicationSearchView;
import org.hesperides.domain.platforms.queries.views.ApplicationView;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.domain.platforms.queries.views.SearchPlatformView;
import org.hesperides.presentation.io.platforms.ApplicationOutput;
import org.hesperides.presentation.io.platforms.PlatformInput;
import org.hesperides.presentation.io.platforms.PlatformOutput;
import org.hesperides.presentation.io.platforms.SearchOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.domain.security.User.fromAuthentication;

@Slf4j
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
    @ApiOperation("Get application")
    public ResponseEntity<ApplicationOutput> getApplication(@PathVariable("application_name") final String applicationName) {
        ApplicationView applicationView = platformUseCases.getApplication(applicationName);
        ApplicationOutput applicationOutput = new ApplicationOutput(applicationView);
        return ResponseEntity.ok(applicationOutput);
    }

    @PostMapping("/{application_name}/platforms")
    @ApiOperation("Create platform")
    public ResponseEntity<PlatformOutput> createPlatform(Authentication authentication,
                                                         @PathVariable("application_name") final String applicationName,
                                                         @Valid @RequestBody final PlatformInput platformInput) {

        Platform platform = platformInput.toDomainInstance();
        Platform.Key createdPlatformKey = platformUseCases.createPlatform(platform, fromAuthentication(authentication));

        PlatformView platformView = platformUseCases.getPlatform(createdPlatformKey);
        PlatformOutput platformOutput = new PlatformOutput(platformView);

        return ResponseEntity.ok(platformOutput);
    }

    @ApiOperation("Retrieve a platform")
    @GetMapping("/{application_name}/platforms/{platform_name}")
    public ResponseEntity<PlatformOutput> getPlatform(@PathVariable("application_name") final String applicationName,
                                                      @PathVariable("platform_name") final String platformName) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        PlatformView platformView = platformUseCases.getPlatform(platformKey);
        PlatformOutput platformOutput = new PlatformOutput(platformView);

        return ResponseEntity.ok(platformOutput);
    }

    @ApiOperation("Update a platform")
    @PutMapping("/{application_name}/platforms")
    public ResponseEntity<PlatformOutput> updatePlatform(Authentication authentication,
                                                         @PathVariable("application_name") final String applicationName,
                                                         @RequestParam(value = "copyPropertiesForUpgradedModules", required = false) final Boolean copyProperties,
                                                         @Valid @RequestBody final PlatformInput platformInput) {

        final boolean copyRequested = Boolean.TRUE.equals(copyProperties); // no null anymore
        Platform.Key platformKey = new Platform.Key(applicationName, platformInput.getPlatformName());

        platformUseCases.updatePlatform(platformKey,
                platformInput.toDomainInstance(),
                copyRequested,
                fromAuthentication(authentication)
        );

        final ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.OK);
        if (copyRequested) {
            // TODO remove as soon as properties are handled
            response.header("x-hesperides-warning", "no property copied! (not implemented yet)");
        }

        PlatformView platformView = platformUseCases.getPlatform(platformKey);
        return response.body(new PlatformOutput(platformView));
    }

    @ApiOperation("Delete a platform")
    @DeleteMapping("/{application_name}/platforms/{platform_name}")
    public ResponseEntity deletePlatform(Authentication authentication,
                                         @PathVariable("application_name") final String applicationName,
                                         @PathVariable("platform_name") final String platformName) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        platformUseCases.deletePlatform(platformKey, fromAuthentication(authentication));
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Search one/all platform")
    @PostMapping("/platforms/perform_search")
    public ResponseEntity<List<SearchOutput>> search(@RequestParam("application_name") final String applicationName,
                                                     @RequestParam(value = "platform_name", required = false) final String platformName) {

        this.checkQueryParameterNotEmpty("application_name", applicationName);
        String platformName2 = platformName == null ? "" : platformName;

        String msg = !StringUtils.isBlank(platformName)
                ? "Search platform " + platformName
                : "Search all platform";
        msg += " from application " + applicationName;
        log.debug(msg);

        List<SearchPlatformView> searchPlatformViewList = platformUseCases.search(applicationName, platformName2);
        List<SearchPlatformOutput> searchPlatformOutputList = searchPlatformViewList != null
                ? searchPlatformViewList.stream().map(SearchPlatformOutput::new).collect(Collectors.toList())
                : new ArrayList<>();

        return ResponseEntity.ok(searchPlatformOutputList);
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
