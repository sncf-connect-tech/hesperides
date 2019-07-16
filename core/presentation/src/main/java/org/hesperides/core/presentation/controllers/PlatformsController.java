package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.SearchPlatformResultView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.GlobalPropertyUsageView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Api(tags = "4. Platforms", description = " ")
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
    @Deprecated
    public ResponseEntity<PlatformIO> createPlatformOld(Authentication authentication,
                                                        @PathVariable("application_name") final String applicationName,
                                                        @RequestParam(value = "from_application", required = false) final String fromApplication,
                                                        @RequestParam(value = "from_platform", required = false) final String fromPlatform,
                                                        @RequestParam(value = "copy_instances_and_properties", defaultValue = "true", required = false) final boolean copyInstancesAndProperties,
                                                        @Valid @RequestBody final PlatformIO platformInput) {
        return createPlatform(authentication, fromApplication, fromPlatform, copyInstancesAndProperties, platformInput);
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
                                                  @RequestParam(value = "timestamp", required = false) final Long timestamp) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        PlatformView platformView;
        if (timestamp != null) {
            platformView = platformUseCases.getPlatformAtPointInTime(platformKey, timestamp);
        } else {
            platformView = platformUseCases.getPlatform(platformKey);
        }
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
    public ResponseEntity deletePlatform(Authentication authentication,
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

    @GetMapping("/{application_name}/platforms/{platform_name}/properties/instance_model")
    @ApiOperation("Get properties with the given path in a platform")
    public ResponseEntity<InstancesModelOutput> getInstancesModel(@PathVariable("application_name") final String applicationName,
                                                                  @PathVariable("platform_name") final String platform_name,
                                                                  @RequestParam("path") final String propertiesPath) {

        Platform.Key platformKey = new Platform.Key(applicationName, platform_name);

        List<String> instancesModelView = platformUseCases.getInstancesModel(platformKey, propertiesPath);
        InstancesModelOutput instancesModelOutput = InstancesModelOutput.fromInstancesModelView(instancesModelView);
        return ResponseEntity.ok(instancesModelOutput);
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

    @ApiOperation("Deprecated - Use GET /applications/platforms/perform_search instead")
    @PostMapping("/platforms/perform_search")
    @Deprecated
    public ResponseEntity<List<SearchResultOutput>> postSearchPlatforms(@RequestParam("applicationName") final String applicationName,
                                                                        @RequestParam(value = "platformName", required = false) final String platformName) {
        return searchPlatforms(applicationName, platformName);
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

    @ApiOperation("List all platform global properties usage")
    @GetMapping("/{application_name}/platforms/{platform_name}/global_properties_usage")
    public ResponseEntity<Map<String, Set<GlobalPropertyUsageOutput>>> getGlobalPropertiesUsage(@PathVariable("application_name") final String applicationName,
                                                                                                @PathVariable("platform_name") final String platformName) {

        Map<String, Set<GlobalPropertyUsageView>> globalPropertyUsageView = platformUseCases.getGlobalPropertiesUsage(new Platform.Key(applicationName, platformName));

        return ResponseEntity.ok(globalPropertyUsageView.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(globalPropertyUsage -> new GlobalPropertyUsageOutput(
                                !globalPropertyUsage.isRemovedFromTemplate(),
                                globalPropertyUsage.getPropertiesPath()))
                        .collect(Collectors.toSet()))));
    }

    @ApiOperation("Get properties with the given path in a platform")
    @GetMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> getValuedProperties(Authentication authentication,
                                                            @PathVariable("application_name") final String applicationName,
                                                            @PathVariable("platform_name") final String platformName,
                                                            @RequestParam("path") final String propertiesPath,
                                                            @RequestParam(value = "timestamp", required = false) final Long timestamp) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        List<AbstractValuedPropertyView> abstractValuedPropertyViews = platformUseCases.getValuedProperties(platformKey, propertiesPath, timestamp, new User(authentication));

        return ResponseEntity.ok(new PropertiesIO(abstractValuedPropertyViews));
    }

    @ApiOperation("Save properties in a platform with the given path")
    @PostMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> saveProperties(Authentication authentication,
                                                       @PathVariable("application_name") final String applicationName,
                                                       @PathVariable("platform_name") final String platformName,
                                                       @RequestParam("path") final String propertiesPath,
                                                       @RequestParam("platform_vid") final Long platformVersionId,
                                                       @Valid @RequestBody final PropertiesIO properties) {
        List<AbstractValuedProperty> abstractValuedProperties = properties.toDomainInstances();
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        List<AbstractValuedPropertyView> propertyViews = platformUseCases.saveProperties(platformKey, propertiesPath, platformVersionId, abstractValuedProperties, new User(authentication));

        return ResponseEntity.ok(new PropertiesIO(propertyViews));
    }
}
