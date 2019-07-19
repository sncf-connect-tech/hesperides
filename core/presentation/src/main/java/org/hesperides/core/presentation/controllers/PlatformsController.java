package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.GlobalPropertyUsageView;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.cache.GetAllApplicationsCacheConfiguration;
import org.hesperides.core.presentation.io.platforms.*;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.hesperides.core.domain.security.User.fromAuthentication;

@Slf4j
@Api(tags = "3. Platforms", description = " ")
@RequestMapping("/applications")
@RestController
public class PlatformsController extends AbstractController {

    private final PlatformUseCases platformUseCases;

    @Autowired
    public PlatformsController(PlatformUseCases platformUseCases) {
        this.platformUseCases = platformUseCases;
    }

    @GetMapping("")
    @ApiOperation("Get applications")
    public ResponseEntity<List<SearchResultOutput>> getApplications() {
        List<SearchApplicationResultView> applications = platformUseCases.getApplicationNames();

        List<SearchResultOutput> applicationOutputs = applications.stream()
                .distinct()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(applicationOutputs);
    }

    @GetMapping("/{application_name}")
    @ApiOperation("Get application")
    public ResponseEntity<ApplicationOutput> getApplication(@PathVariable("application_name") final String applicationName,
                                                            @RequestParam(value = "hide_platform", required = false) final Boolean hidePlatformsModules) {

        ApplicationView applicationView = platformUseCases.getApplication(applicationName);
        ApplicationOutput applicationOutput = new ApplicationOutput(applicationView, Boolean.TRUE.equals(hidePlatformsModules));

        return ResponseEntity.ok(applicationOutput);
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
            platformId = platformUseCases.createPlatform(newPlatform, fromAuthentication(authentication));
        } else {
            checkQueryParameterNotEmpty("from_application", fromApplication);
            checkQueryParameterNotEmpty("from_platform", fromPlatform);
            Platform.Key existingPlatformKey = new Platform.Key(fromApplication, fromPlatform);
            platformId = platformUseCases.copyPlatform(newPlatform, existingPlatformKey, copyInstancesAndProperties, fromAuthentication(authentication));
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
                                                     @RequestParam(value = "copyPropertiesForUpgradedModules", required = false) final Boolean copyPropertiesForUpgradedModules,
                                                     @Valid @RequestBody final PlatformIO platformInput) {

        Platform.Key platformKey = new Platform.Key(applicationName, platformInput.getPlatformName());

        platformUseCases.updatePlatform(platformKey,
                platformInput.toDomainInstance(),
                Boolean.TRUE.equals(copyPropertiesForUpgradedModules), // on traite le cas `null`
                fromAuthentication(authentication)
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
        platformUseCases.deletePlatform(platformKey, fromAuthentication(authentication));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{application_name}/platforms/{platform_name}/restore")
    @ApiOperation("Restore platform")
    public ResponseEntity<PlatformIO> restorePlatform(Authentication authentication,
                                                      @PathVariable("application_name") final String applicationName,
                                                      @PathVariable("platform_name") final String platformName) {
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        PlatformView platformView = platformUseCases.restoreDeletedPlatform(platformKey, fromAuthentication(authentication));
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

    @ApiOperation("Deprecated - Use GET /applications/perform_search instead")
    @PostMapping("/perform_search")
    @Deprecated
    public ResponseEntity<List<SearchResultOutput>> postSearchApplications(@RequestParam("name") String applicationName) {
        return searchApplications(applicationName);
    }

    @ApiOperation("Search applications")
    @GetMapping("/perform_search")
    public ResponseEntity<List<SearchResultOutput>> searchApplications(@RequestParam("name") String applicationName) {

        List<SearchApplicationResultView> searchApplicationResultViews = platformUseCases.searchApplications(defaultString(applicationName, ""));

        List<SearchResultOutput> searchResultOutputs = Optional.ofNullable(searchApplicationResultViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .distinct()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(searchResultOutputs);
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

    @ApiOperation("Get all applications, their platforms and their modules (with a cache)")
    @GetMapping("/platforms")
    @Cacheable(GetAllApplicationsCacheConfiguration.CACHE_NAME)
    public ResponseEntity<AllApplicationsDetailOutput> getAllApplicationsDetail() {

        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(utcTimeZone);
        String nowAsIso = df.format(new Date());

        final List<ApplicationOutput> applications = platformUseCases.getAllApplicationsDetail()
                .stream()
                .map(application -> new ApplicationOutput(application, false))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new AllApplicationsDetailOutput(nowAsIso, applications));
    }

}
