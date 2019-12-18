package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.SearchPlatformResultView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
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

@Slf4j
@Api(tags = "04. Platforms", description = " ")
@RequestMapping("/applications")
@RestController
public class PlatformsController extends AbstractController {

    private final PlatformUseCases platformUseCases;

    @Autowired
    public PlatformsController(PlatformUseCases platformUseCases) {
        this.platformUseCases = platformUseCases;
    }

    @Deprecated
    @ApiOperation("Deprecated - Use POST /applications")
    @PostMapping("/{application_name}/platforms")
    public ResponseEntity<PlatformIO> createPlatformOld(Authentication authentication,
                                                        @PathVariable("application_name") final String applicationName,
                                                        @RequestParam(value = "from_application", required = false) final String fromApplication,
                                                        @RequestParam(value = "from_platform", required = false) final String fromPlatform,
                                                        @RequestParam(value = "copy_instances_and_properties", defaultValue = "true", required = false) final boolean copyInstancesAndProperties,
                                                        @Valid @RequestBody final PlatformIO platformInput) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-05-03\"")
                .header("Sunset", "Sat May  4 00:00:00 CEST 2020")
                .header("Link", "/applications")
                .body(createPlatform(authentication, fromApplication, fromPlatform, copyInstancesAndProperties, platformInput).getBody());
    }

    @PostMapping
    @ApiOperation("Create platform")
    public ResponseEntity<PlatformIO> createPlatform(Authentication authentication,
                                                     @RequestParam(value = "from_application", required = false) final String fromApplication,
                                                     @RequestParam(value = "from_platform", required = false) final String fromPlatform,
                                                     @RequestParam(value = "copy_instances_and_properties", defaultValue = "true", required = false) final boolean copyInstancesAndProperties,
                                                     @Valid @RequestBody final PlatformIO platformInput) {

        Platform newPlatform = platformInput.toDomainInstance();

        String platformId;
        if (StringUtils.isBlank(fromApplication) && StringUtils.isBlank(fromPlatform)) {
            platformId = platformUseCases.createPlatform(newPlatform, new User(authentication));
        } else {
            checkQueryParameterNotEmpty("from_application", fromApplication);
            checkQueryParameterNotEmpty("from_platform", fromPlatform);
            Platform.Key existingPlatformKey = new Platform.Key(fromApplication, fromPlatform);
            platformId = platformUseCases.copyPlatform(newPlatform, existingPlatformKey, copyInstancesAndProperties, new User(authentication));
        }
        PlatformView platformView = platformUseCases.getPlatform(platformId);
        PlatformIO platformOutput = new PlatformIO(platformView);

        return ResponseEntity.ok(platformOutput);
    }

    @ApiOperation("Retrieve a platform")
    @GetMapping("/{application_name}/platforms/{platform_name:.+}")
    public ResponseEntity<PlatformIO> getPlatform(@PathVariable("application_name") final String applicationName,
                                                  @PathVariable("platform_name") final String platformName,
                                                  @ApiParam(value = "En milliseconds depuis l'EPOCH. Pour le générer via Javascript à partir d'une date: new Date('2019-01-01 12:00:00').getTime()")
                                                  @RequestParam(value = "timestamp", required = false) final Long timestamp,
                                                  @RequestParam(value = "with_password_info", required = false) final Boolean withPasswordFlag) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        PlatformView platformView = timestamp == null
                ? platformUseCases.getPlatform(platformKey, Boolean.TRUE.equals(withPasswordFlag))
                : platformUseCases.getPlatformAtPointInTime(platformKey, timestamp);

        PlatformIO platformOutput = new PlatformIO(platformView);
        return ResponseEntity.ok(platformOutput);
    }

    @ApiOperation("Update a platform")
    @PutMapping("/{application_name}/platforms")
    public ResponseEntity<PlatformIO> updatePlatform(Authentication authentication,
                                                     @PathVariable("application_name") final String applicationName,
                                                     @ApiParam(value = "Copie les propriétés du module déployé de la plateforme source avec l'ID correspondant. " +
                                                             "Si ce module ne contient pas de propriétés, à défaut on utilise les propriétés du module avec le même properties_path.")
                                                     @RequestParam(value = "copyPropertiesForUpgradedModules", required = false) final Boolean copyPropertiesForUpgradedModules,
                                                     @Valid @RequestBody final PlatformIO platformInput) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformInput.getPlatformName());

        platformUseCases.updatePlatform(platformKey,
                platformInput.toDomainInstance(),
                Boolean.TRUE.equals(copyPropertiesForUpgradedModules), // on traite le cas `null`
                new User(authentication)
        );

        final ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.OK);
        PlatformView platformView = platformUseCases.getPlatform(platformKey);
        return response.body(new PlatformIO(platformView));
    }

    @ApiOperation("Delete a platform")
    @DeleteMapping("/{application_name}/platforms/{platform_name}")
    public ResponseEntity<Void> deletePlatform(Authentication authentication,
                                               @PathVariable("application_name") final String applicationName,
                                               @PathVariable("platform_name") final String platformName) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        platformUseCases.deletePlatform(platformKey, new User(authentication));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{application_name}/platforms/{platform_name}/restore")
    @ApiOperation("Restore a deleted platform")
    public ResponseEntity<PlatformIO> restorePlatform(Authentication authentication,
                                                      @PathVariable("application_name") final String applicationName,
                                                      @PathVariable("platform_name") final String platformName) {
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        PlatformView platformView = platformUseCases.restoreDeletedPlatform(platformKey, new User(authentication));
        PlatformIO platformOutput = new PlatformIO(platformView);
        return ResponseEntity.ok(platformOutput);
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
        List<ModulePlatformView> modulePlatformViews = platformUseCases.getPlatformsUsingModule(moduleKey);
        List<ModulePlatformsOutput> modulePlatformsOutputs = ModulePlatformsOutput.fromViews(modulePlatformViews);

        return ResponseEntity.ok(modulePlatformsOutputs);
    }

    @Deprecated
    @ApiOperation("Deprecated - Use GET /applications/platforms/perform_search instead")
    @PostMapping("/platforms/perform_search")
    public ResponseEntity<List<SearchResultOutput>> postSearchPlatforms(@RequestParam("applicationName") final String applicationName,
                                                                        @RequestParam(value = "platformName", required = false) final String platformName) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-04-23\"")
                .header("Sunset", "Wed Apr 24 00:00:00 CEST 2020")
                .header("Link", "/applications/platforms/perform_search")
                .body(searchPlatforms(applicationName, platformName).getBody());
    }

    @ApiOperation("List platforms of a given application")
    @GetMapping("/platforms/perform_search")
    public ResponseEntity<List<SearchResultOutput>> searchPlatforms(@RequestParam("applicationName") final String applicationName,
                                                                    @RequestParam(value = "platformName", required = false) final String platformName) {

        checkQueryParameterNotEmpty("applicationName", applicationName);
        List<SearchPlatformResultView> searchPlatformResultViews = platformUseCases.searchPlatforms(applicationName, platformName);

        List<SearchResultOutput> searchResultOutputs = Optional.ofNullable(searchPlatformResultViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .distinct()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(searchResultOutputs);
    }
}
