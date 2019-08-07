package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.presentation.io.platforms.InstancesModelOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@Api(tags = "4. Properties", description = " ")
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
                                                            @RequestParam(value = "timestamp", required = false) final Long timestamp) {
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);

        List<AbstractValuedPropertyView> abstractValuedPropertyViews = platformUseCases.getValuedProperties(platformKey, propertiesPath, timestamp, new User(authentication));
        return ResponseEntity.ok(new PropertiesIO(platformUseCases.getPropertiesVersionId(platformKey, propertiesPath, timestamp), abstractValuedPropertyViews));
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
                                                       @Valid @RequestBody final PropertiesIO properties) {
        return updateProperties(authentication, applicationName, platformName, propertiesPath, platformVersionId, properties);
    }


    @ApiOperation("Update deployed modules or global properties")
    @PutMapping("/{application_name}/platforms/{platform_name}/properties")
    public ResponseEntity<PropertiesIO> updateProperties(Authentication authentication,
                                                         @PathVariable("application_name") final String applicationName,
                                                         @PathVariable("platform_name") final String platformName,
                                                         @RequestParam("path") final String propertiesPath,
                                                         @RequestParam("platform_vid") final Long platformVersionId,
                                                         @Valid @RequestBody final PropertiesIO properties) {
        List<AbstractValuedProperty> abstractValuedProperties = properties.toDomainInstances();
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);

        List<AbstractValuedPropertyView> propertyViews = platformUseCases.saveProperties(platformKey, propertiesPath, platformVersionId, abstractValuedProperties, properties.getPropertiesVersionId(), new User(authentication));
        final Long propertiesVersionId = platformUseCases.getPropertiesVersionId(platformKey, propertiesPath);
        return ResponseEntity.ok(new PropertiesIO(propertiesVersionId, propertyViews));
    }
}
