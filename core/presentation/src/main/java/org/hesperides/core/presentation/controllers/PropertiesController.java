package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.PropertyWithDetails;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.GlobalPropertyUsageView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.properties.BasicPropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesWithDetailsIO;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "05. Properties", description = " ")
@RequestMapping("/applications")
@RestController
public class PropertiesController extends AbstractController {

    private final PlatformUseCases platformUseCases;

    @Autowired
    public PropertiesController(PlatformUseCases platformUseCases) {
        this.platformUseCases = platformUseCases;
    }

    @ApiOperation("Get properties with the given path in a platform")
    @GetMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> getValuedProperties(Authentication authentication,
                                                            @PathVariable("application_name") final String applicationName,
                                                            @PathVariable("platform_name") final String platformName,
                                                            @RequestParam("path") final String propertiesPath,
                                                            @ApiParam(value = "En milliseconds depuis l'EPOCH. Pour le générer via Javascript à partir d'une date: new Date('2019-01-01 12:00:00').getTime()")
                                                            @RequestParam(value = "timestamp", required = false) final Long timestamp,
                                                            @RequestParam(value = "with_details", required = false) final boolean withDetails) {
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);

        Long propertiesVersionId = platformUseCases.getPropertiesVersionId(platformKey, propertiesPath, timestamp);
        List<AbstractValuedPropertyView> abstractValuedPropertyViews = platformUseCases.getValuedProperties(platformKey, propertiesPath, timestamp, new User(authentication));
        PropertiesIO propertiesIO;
        if (withDetails) {
            List<PropertyWithDetails> propertiesWithDetails = platformUseCases.getPropertiesWithDetails(platformKey, propertiesPath, new User(authentication));

            propertiesIO = new PropertiesWithDetailsIO(propertiesVersionId, new HashSet(propertiesWithDetails), abstractValuedPropertyViews);
        } else {
            abstractValuedPropertyViews = platformUseCases.getValuedProperties(platformKey, propertiesPath, timestamp, new User(authentication));
            propertiesIO = new BasicPropertiesIO(propertiesVersionId, abstractValuedPropertyViews);
        }
        return ResponseEntity.ok(propertiesIO);
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

    @Deprecated
    @ApiOperation("Deprecated - Use PUT /{application_name}/platforms/{platform_name}/properties instead")
    @PostMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> saveProperties(Authentication authentication,
                                                       @PathVariable("application_name") final String applicationName,
                                                       @PathVariable("platform_name") final String platformName,
                                                       @RequestParam("path") final String propertiesPath,
                                                       @RequestParam("platform_vid") final Long platformVersionId,
                                                       @Valid @RequestBody final BasicPropertiesIO properties) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-08-02\"")
                .header("Sunset", "Sat Aug  3 00:00:00 CEST 2020")
                .header("Link", String.format("/applications/%s/platforms/%s/properties", applicationName, platformName))
                .body(updateProperties(authentication, applicationName, platformName, propertiesPath, platformVersionId, properties).getBody());
    }

    @ApiOperation("Update deployed modules or global properties")
    @PutMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> updateProperties(Authentication authentication,
                                                         @PathVariable("application_name") final String applicationName,
                                                         @PathVariable("platform_name") final String platformName,
                                                         @RequestParam("path") final String propertiesPath,
                                                         @RequestParam("platform_vid") final Long platformVersionId,
                                                         @Valid @RequestBody final BasicPropertiesIO properties) {

        List<AbstractValuedProperty> abstractValuedProperties = properties.toDomainInstances();
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);

        List<AbstractValuedPropertyView> propertyViews = platformUseCases.saveProperties(platformKey, propertiesPath, platformVersionId, abstractValuedProperties, properties.getPropertiesVersionId(), new User(authentication));
        final Long propertiesVersionId = platformUseCases.getPropertiesVersionId(platformKey, propertiesPath);
        return ResponseEntity.ok(new BasicPropertiesIO(propertiesVersionId, propertyViews));
    }

    @ApiOperation("Get properties diff with the given paths in given platforms")
    @GetMapping("/{application_name}/platforms/{platform_name}/properties/diff")
    public ResponseEntity<PropertiesDiffOutput> getPropertiesDiff(Authentication authentication,
                                                                  @PathVariable("application_name") final String fromApplicationName,
                                                                  @PathVariable("platform_name") final String fromPlatformName,
                                                                  @RequestParam("path") final String fromPropertiesPath,
                                                                  @RequestParam(value = "instance_name", required = false, defaultValue = "") final String fromInstanceName,
                                                                  @RequestParam("to_application") final String toApplicationName,
                                                                  @RequestParam("to_platform") final String toPlatformName,
                                                                  @RequestParam("to_path") final String toPropertiesPath,
                                                                  @RequestParam(value = "to_instance_name", required = false, defaultValue = "") final String toInstanceName,
                                                                  @RequestParam(value = "compare_stored_values", required = false) final boolean compareStoredValues,
                                                                  @ApiParam(value = "En milliseconds depuis l'EPOCH. Pour le générer via Javascript à partir d'une date: new Date('2019-01-01 12:00:00').getTime()")
                                                                  @RequestParam(value = "timestamp", required = false) final Long timestamp) {
        Platform.Key fromPlatformKey = new Platform.Key(fromApplicationName, fromPlatformName);
        Platform.Key toPlatformKey = new Platform.Key(toApplicationName, toPlatformName);

        PropertiesDiff propertiesDiff = platformUseCases.getPropertiesDiff(
                fromPlatformKey, fromPropertiesPath, fromInstanceName,
                toPlatformKey, toPropertiesPath, toInstanceName,
                timestamp, compareStoredValues,
                new User(authentication));
        return ResponseEntity.ok(new PropertiesDiffOutput(propertiesDiff));
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
}
