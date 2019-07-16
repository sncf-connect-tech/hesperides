package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.presentation.io.platforms.DeployedModuleIO;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.security.User.fromAuthentication;


@Slf4j
@Api(tags = "3. Platforms", description = " ")
@RequestMapping("/applications")
@RestController
public class DeployedModulesController extends AbstractController {

    private final PlatformUseCases platformUseCases;

    @Autowired
    public DeployedModulesController(PlatformUseCases platformUseCases) {
        this.platformUseCases = platformUseCases;
    }

    @ApiOperation("Update deployed modules properties")
    @PostMapping("/{application_name}/platforms/{platform_name}/deployed_modules/properties")
    public ResponseEntity<PropertiesIO> updateProperties(Authentication authentication,
                                                       @PathVariable("application_name") final String applicationName,
                                                       @PathVariable("platform_name") final String platformName,
                                                       @RequestParam("path") final String propertiesPath,
                                                       @RequestParam("platform_vid") final Long platformVersionId,
                                                       @Valid @RequestBody final PropertiesIO properties) {
        List<AbstractValuedProperty> abstractValuedProperties = properties.toDomainInstances();
        Platform.Key platformKey = new Platform.Key(applicationName, platformName);
        List<AbstractValuedPropertyView> propertyViews = platformUseCases.saveProperties(platformKey, propertiesPath, platformVersionId, abstractValuedProperties, properties.getDeployedModuleVersionId(), fromAuthentication(authentication));

        return ResponseEntity.ok(new PropertiesIO(propertyViews));
    }
}