package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.application.platforms.PropertiesUseCases;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.queries.views.PropertiesEventView;
import org.hesperides.core.domain.platforms.queries.views.properties.*;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.PropertiesEventOutput;
import org.hesperides.core.presentation.io.platforms.properties.*;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff.ComparisonMode;

@Slf4j
@Api(tags = "05. Properties", description = " ")
@RequestMapping("/applications")
@RestController
public class PropertiesController extends AbstractController {

    private final PropertiesUseCases propertiesUseCases;
    private final PlatformUseCases platformUseCases;

    @Autowired
    public PropertiesController(PropertiesUseCases propertiesUseCases, PlatformUseCases platformUseCases) {
        this.propertiesUseCases = propertiesUseCases;
        this.platformUseCases = platformUseCases;
    }

    @ApiOperation("Get properties with the given path in a platform")
    @GetMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> getValuedProperties(Authentication authentication,
                                                            @PathVariable("application_name") final String applicationName,
                                                            @PathVariable("platform_name") final String platformName,
                                                            @RequestParam("path") final String propertiesPath,
                                                            @ApiParam(value = "En milliseconds depuis l'EPOCH. Pour le générer via Javascript à partir d'une date: new Date('2019-01-01 12:00:00').getTime()")
                                                            @RequestParam(value = "timestamp", required = false) final Long timestamp) {
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        User authenticatedUser = new User(authentication);
        Long propertiesVersionId = propertiesUseCases.getPropertiesVersionId(platformKey, propertiesPath, timestamp);
        List<AbstractValuedPropertyView> allValuedProperties = propertiesUseCases.getValuedProperties(platformKey, propertiesPath, timestamp, authenticatedUser);
        PropertiesIO propertiesIO = new PropertiesIO(propertiesVersionId, allValuedProperties);
        return ResponseEntity.ok(propertiesIO);
    }

    @GetMapping("/{application_name}/platforms/{platform_name}/properties/instance_model")
    @ApiOperation("Get properties with the given path in a platform")
    public ResponseEntity<InstancesModelOutput> getInstancesModel(@PathVariable("application_name") String applicationName,
                                                                  @PathVariable("platform_name") String platform_name,
                                                                  @RequestParam("path") String propertiesPath) {

        Platform.Key platformKey = new Platform.Key(applicationName, platform_name);
        List<String> instancesModelView = propertiesUseCases.getInstancesModel(platformKey, propertiesPath);
        InstancesModelOutput instancesModelOutput = InstancesModelOutput.fromInstancesModelView(instancesModelView);
        return ResponseEntity.ok(instancesModelOutput);
    }

    @Deprecated
    @ApiOperation("Deprecated - Use PUT /{application_name}/platforms/{platform_name}/properties instead")
    @PostMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> saveProperties(Authentication authentication,
                                                       @PathVariable("application_name") String applicationName,
                                                       @PathVariable("platform_name") String platformName,
                                                       @RequestParam("path") String propertiesPath,
                                                       @RequestParam("platform_vid") Long platformVersionId,
                                                       @RequestParam(value = "comment", required = false) String userComment,
                                                       @Valid @RequestBody PropertiesIO properties) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-08-02\"")
                .header("Sunset", "Sat Aug  3 00:00:00 CEST 2020")
                .header("Link", String.format("/applications/%s/platforms/%s/properties", applicationName, platformName))
                .body(updateProperties(
                        authentication,
                        applicationName,
                        platformName,
                        propertiesPath,
                        platformVersionId,
                        userComment,
                        properties).getBody());
    }

    @ApiOperation("Update deployed modules or global properties")
    @PutMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> updateProperties(Authentication authentication,
                                                         @PathVariable("application_name") String applicationName,
                                                         @PathVariable("platform_name") String platformName,
                                                         @RequestParam("path") String propertiesPath,
                                                         @RequestParam("platform_vid") Long platformVersionId,
                                                         @RequestParam(value = "comment", required = false) String userComment,
                                                         @Valid @RequestBody PropertiesIO properties) {

        List<AbstractValuedProperty> abstractValuedProperties = properties.toDomainInstances();
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);

        List<AbstractValuedPropertyView> propertyViews = propertiesUseCases.saveProperties(
                platformKey,
                propertiesPath,
                platformVersionId,
                abstractValuedProperties,
                properties.getPropertiesVersionId(),
                userComment,
                new User(authentication));
        Long propertiesVersionId = propertiesUseCases.getPropertiesVersionId(platformKey, propertiesPath, null);
        return ResponseEntity.ok(new PropertiesIO(propertiesVersionId, propertyViews));
    }

    @ApiOperation("Get properties diff with the given paths in given platforms")
    @GetMapping("/{application_name}/platforms/{platform_name}/properties/diff")
    public ResponseEntity<PropertiesDiffOutput> getPropertiesDiff(Authentication authentication,
                                                                  @PathVariable("application_name") String fromApplicationName,
                                                                  @PathVariable("platform_name") String fromPlatformName,
                                                                  @RequestParam("path") String fromPropertiesPath,
                                                                  @RequestParam(value = "instance_name", required = false, defaultValue = "") String fromInstanceName,
                                                                  @RequestParam("to_application") String toApplicationName,
                                                                  @RequestParam("to_platform") String toPlatformName,
                                                                  @RequestParam("to_path") String toPropertiesPath,
                                                                  @RequestParam(value = "to_instance_name", required = false, defaultValue = "") String toInstanceName,
                                                                  @RequestParam(value = "compare_stored_values", required = false) boolean compareStoredValues,
                                                                  @ApiParam(value = "En milliseconds depuis l'EPOCH. Correspond au champ \"left\" de la sortie JSON. Pour le générer via Javascript à partir d'une date: new Date('2019-01-01 12:00:00').getTime()")
                                                                  @RequestParam(value = "timestamp", required = false) Long timestamp,
                                                                  @ApiParam(value = "En milliseconds depuis l'EPOCH. Correspond au champ \"right\" de la sortie JSON. Pour le générer via Javascript à partir d'une date: new Date('2019-01-01 12:00:00').getTime()")
                                                                  @RequestParam(value = "origin_timestamp", required = false) Long originTimestamp) {
        Platform.Key fromPlatformKey = new Platform.Key(fromApplicationName, fromPlatformName);
        Platform.Key toPlatformKey = new Platform.Key(toApplicationName, toPlatformName);

        PropertiesDiff propertiesDiff = propertiesUseCases.getPropertiesDiff(
                fromPlatformKey, fromPropertiesPath, fromInstanceName,
                toPlatformKey, toPropertiesPath, toInstanceName,
                timestamp, originTimestamp, compareStoredValues ? ComparisonMode.STORED : ComparisonMode.FINAL,
                new User(authentication));
        return ResponseEntity.ok(new PropertiesDiffOutput(propertiesDiff));
    }

    @ApiOperation("List all platform global properties usage")
    @GetMapping("/{application_name}/platforms/{platform_name}/global_properties_usage")
    public ResponseEntity<Map<String, Set<GlobalPropertyUsageOutput>>> getGlobalPropertiesUsage(@PathVariable("application_name") String applicationName,
                                                                                                @PathVariable("platform_name") String platformName) {

        Map<String, Set<GlobalPropertyUsageView>> globalPropertyUsageView = propertiesUseCases.getGlobalPropertiesUsage(new Platform.Key(applicationName, platformName));

        return ResponseEntity.ok(globalPropertyUsageView.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(globalPropertyUsage -> new GlobalPropertyUsageOutput(
                                !globalPropertyUsage.isRemovedFromTemplate(),
                                globalPropertyUsage.getPropertiesPath()))
                        .collect(Collectors.toSet()))));
    }

    @ApiOperation("Purge properties that are no longer needed by related templates")
    @DeleteMapping("/{application_name}/platforms/{platform_name}/properties/clean_unused_properties")
    public ResponseEntity<Void> cleanUnusedProperties(Authentication authentication,
                                                      @PathVariable("application_name") String applicationName,
                                                      @PathVariable("platform_name") String platformName,
                                                      @RequestParam(value = "properties_path", required = false) String propertiesPath) {
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        User authenticatedUser = new User(authentication);

        if (StringUtils.isEmpty(propertiesPath)) {
            // tous les modules
            platformUseCases.getPlatform(platformKey).findActiveDeployedModules()
                    .forEach(module -> propertiesUseCases.purgeUnusedProperties(platformKey, module.getPropertiesPath(), authenticatedUser));
        } else {
            // un seul module
            propertiesUseCases.purgeUnusedProperties(platformKey, propertiesPath, authenticatedUser);
        }

        return ResponseEntity.noContent().build();
    }

    @ApiOperation("Get detailed properties of a platform or a deployed module")
    @GetMapping("/{application_name}/platforms/{platform_name}/detailed_properties")
    public ResponseEntity<PlatformDetailedPropertiesOutput> getDetailedProperties(Authentication authentication,
                                                                                  @PathVariable("application_name") final String applicationName,
                                                                                  @PathVariable("platform_name") final String platformName,
                                                                                  @RequestParam(value = "properties_path", required = false) final String propertiesPath) {
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        User user = new User(authentication);
        PlatformDetailedPropertiesView platformDetailedPropertiesView = propertiesUseCases.getDetailedProperties(platformKey, propertiesPath, user);
        PlatformDetailedPropertiesOutput platformDetailedPropertiesOutput = new PlatformDetailedPropertiesOutput(platformDetailedPropertiesView);
        return ResponseEntity.ok(platformDetailedPropertiesOutput);
    }

    @ApiOperation("Get the history of raw values for module properties or global properties")
    @GetMapping("/{application_name}/platforms/{platform_name:.+}/properties/events")
    public ResponseEntity<List<PropertiesEventOutput>> getPropertiesEvents(Authentication authentication,
                                                                           @PathVariable("application_name") final String applicationName,
                                                                           @PathVariable("platform_name") final String platformName,
                                                                           @RequestParam(value = "properties_path") final String propertiesPath,
                                                                           @RequestParam(value = "page", required = false, defaultValue = "1") final Integer page,
                                                                           @RequestParam(value = "size", required = false, defaultValue = "20") final Integer size) {

        User user = new User(authentication);
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        List<PropertiesEventView> propertiesEventViews = propertiesUseCases.getPropertiesEvents(user, platformKey, propertiesPath, page, size);
        List<PropertiesEventOutput> propertiesEventOutputs = PropertiesEventOutput.fromViews(propertiesEventViews);
        return ResponseEntity.ok(propertiesEventOutputs);
    }

    @ApiIgnore
    @GetMapping("/all_passwords")
    public ResponseEntity<List<?>> getPasswords(Authentication authentication,
                                                @RequestParam(value = "flat", required = false) Boolean flat) {

        User user = new User(authentication);
        List<PlatformPropertiesView> platformsPasswords = propertiesUseCases.findAllApplicationsPasswords(user);
        List<?> passwords;
        if (Boolean.TRUE.equals(flat)) {
            passwords = FlatPasswordsOutput.fromViews(platformsPasswords);
        } else {
            passwords = PlatformPasswordsOutput.fromViews(platformsPasswords);
        }
        return ResponseEntity.ok(passwords);
    }

    @ApiOperation("Search properties by exact name and/or exact value")
    @GetMapping("/search_properties")
    public ResponseEntity<List<PropertySearchResultOutput>> searchProperties(
            Authentication authentication,
            @RequestParam(value = "property_name", required = false) String propertyName,
            @RequestParam(value = "property_value", required = false) String propertyValue) {

        if (isBlank(propertyName) && isBlank(propertyValue)) {
            throw new IllegalArgumentException("You have to search for a property's name and/or value");
        }

        User user = new User(authentication);
        List<PropertySearchResultView> propertyViews = propertiesUseCases.searchProperties(
                propertyName, propertyValue, user);
        List<PropertySearchResultOutput> propertyOutputs = PropertySearchResultOutput.fromViews(propertyViews);

        return ResponseEntity.ok(propertyOutputs);
    }
}
