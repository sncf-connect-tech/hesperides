package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.application.platforms.PlatformUseCases;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.domain.platforms.queries.views.SearchPlatformView;
import org.hesperides.presentation.io.platforms.PlatformIO;
import org.hesperides.presentation.io.platforms.SearchPlatformOutput;
import org.springframework.beans.factory.annotation.Autowired;
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

    @ApiOperation("Search one/all platform")
    @PostMapping("/platforms/perform_search")
    public ResponseEntity<List <SearchPlatformOutput>> search(Authentication authentication,
                                                         @RequestParam("application_name") final String applicationName,
                                                         @RequestParam(value = "platform_name", required = false) final String platformName) {
        // verify parameters.
        this.checkQueryParameterNotEmpty("application_name", applicationName);
        String platformName2 = platformName == null ? "" : platformName;

        String msg = !StringUtils.isBlank(platformName)
                ? "Search platform " + platformName
                : "Search all platform";
        msg += " from application " + applicationName;
        log.debug(msg);

        List <SearchPlatformView> searchPlatformViewList = platformUseCases.search(applicationName, platformName2);
        List <SearchPlatformOutput> searchPlatformOutputList = searchPlatformViewList != null
                ? searchPlatformViewList.stream().map(SearchPlatformOutput::new).collect(Collectors.toList())
                : new ArrayList<>();

        return ResponseEntity.ok(searchPlatformOutputList);
    }
}
