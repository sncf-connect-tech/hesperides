package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.platforms.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.security.User.fromAuthentication;

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

    @GetMapping("/{application_name}")
    @ApiOperation("Get applications")
    public ResponseEntity<ApplicationOutput> getApplication(@PathVariable("application_name") final String applicationName,
                                                            @RequestParam(value = "hide_platform", required = false) final Boolean hidePlatformsModules) {

        ApplicationView applicationView = platformUseCases.getApplication(applicationName);
        ApplicationOutput applicationOutput = new ApplicationOutput(applicationView, Boolean.TRUE.equals(hidePlatformsModules));

        return ResponseEntity.ok(applicationOutput);
    }

    @ApiOperation("Retrieve platforms using module")
    @GetMapping("/using_module/{module_name}/{module_version}/{version_type}")
    public ResponseEntity<List<ModulePlatformsOutput>> getPlatformsUsingModule(@PathVariable("module_name") final String moduleName,
                                                                               @PathVariable("module_version") final String moduleVersion,
                                                                               @PathVariable("version_type") final String moduleVersionType) {

        // Exceptionnellement, le version-type fourni en paramètre est un String car on peut lui attribuer
        // n'importe quelle valeur. Si la valeur n'est pas release, alors on considère que c'est un working-copy.
        boolean isWorkingCopy = !TemplateContainer.VersionType.release.toString().equalsIgnoreCase(moduleVersionType);

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.getVersionType(isWorkingCopy));
        List<ModulePlatformView> modulePlatformViews = platformUseCases.getPlatformUsingModule(moduleKey);
        List<ModulePlatformsOutput> modulePlatformsOutputs = ModulePlatformsOutput.fromViews(modulePlatformViews);

        return ResponseEntity.ok(modulePlatformsOutputs);
    }

    @ApiOperation("List platforms of a given application")
    @PostMapping("/platforms/perform_search")
    public ResponseEntity<List<SearchResultOutput>> searchPlatforms(@RequestParam("application_name") final String applicationName,
                                                                    @RequestParam(value = "platform_name", required = false) final String platformName) {

        this.checkQueryParameterNotEmpty("application_name", applicationName);
        List<SearchPlatformResultView> searchPlatformResultViews = platformUseCases.searchPlatforms(applicationName, platformName);

        List<SearchResultOutput> searchResultOutputs = Optional.ofNullable(searchPlatformResultViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(searchResultOutputs);
    }

    @ApiOperation("Search applications")
    @PostMapping("/perform_search")
    public ResponseEntity<List<SearchResultOutput>> searchApplications(@RequestParam("name") final String applicationName) {

        List<SearchApplicationResultView> searchApplicationResultViews = platformUseCases.searchApplications(applicationName);

        List<SearchResultOutput> searchResultOutputs = Optional.ofNullable(searchApplicationResultViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(searchResultOutputs);
    }
}
