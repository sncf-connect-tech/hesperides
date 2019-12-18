/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.modules.ModuleUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.ModuleKeyOutput;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Api(tags = "01. Modules", description = " ", position = 1)
@RequestMapping("/modules")
@RestController
public class ModulesController extends AbstractController {

    private final ModuleUseCases moduleUseCases;

    @Autowired
    public ModulesController(ModuleUseCases moduleUseCases) {
        this.moduleUseCases = moduleUseCases;
    }

    @ApiOperation("Create a working copy (possibly from a release)")
    @PostMapping
    public ResponseEntity<ModuleIO> createWorkingCopy(Authentication authentication,
                                                      @RequestParam(value = "from_module_name", required = false) final String fromModuleName,
                                                      @RequestParam(value = "from_module_version", required = false) final String fromModuleVersion,
                                                      @RequestParam(value = "from_is_working_copy", required = false) final Boolean isFromWorkingCopy,
                                                      @Valid @RequestBody final ModuleIO moduleInput) {

        log.info("createWorkingCopy {}", moduleInput.toString());

        String moduleId;
        User currentUser = new User(authentication);
        if (StringUtils.isBlank(fromModuleName)
                && StringUtils.isBlank(fromModuleVersion)
                && isFromWorkingCopy == null) {

            moduleId = moduleUseCases.createWorkingCopy(moduleInput.toDomainInstance(), currentUser);

        } else {
            checkQueryParameterNotEmpty("from_module_name", fromModuleName);
            checkQueryParameterNotEmpty("from_module_version", fromModuleVersion);
            checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

            TemplateContainer.Key existingModuleKey = new Module.Key(fromModuleName, fromModuleVersion, TemplateContainer.getVersionType(isFromWorkingCopy));
            moduleId = moduleUseCases.createWorkingCopyFrom(existingModuleKey, moduleInput.toDomainInstance().getKey(), currentUser);
        }

        ModuleView moduleView = moduleUseCases.getModule(moduleId).get();
        ModuleIO moduleOutput = new ModuleIO(moduleView);
        URI moduleUri = moduleView.toDomainInstance().getKey().getURI();
        return ResponseEntity.created(moduleUri).body(moduleOutput);
    }

    @ApiOperation("Update a module working copy")
    @PutMapping
    public ResponseEntity<ModuleIO> updateWorkingCopy(Authentication authentication,
                                                      @Valid @RequestBody final ModuleIO moduleInput) {

        log.info("Updating module workingcopy {}", moduleInput.toString());

        Module module = moduleInput.toDomainInstance();
        moduleUseCases.updateModuleTechnos(module, new User(authentication));
        ModuleIO moduleOutput = moduleUseCases.getModule(module.getKey())
                .map(ModuleIO::new)
                .orElseThrow(() -> new ModuleNotFoundException(module.getKey()));

        return ResponseEntity.ok(moduleOutput);
    }

    @ApiOperation("Get all module names")
    @GetMapping
    public ResponseEntity<List<String>> getModulesName() {

        log.debug("getModulesName");

        List<String> modulesName = moduleUseCases.getModulesName();
        log.debug("return getModulesName: {}", modulesName.toString());

        return ResponseEntity.ok(modulesName);
    }

    @ApiOperation("Get all versions for a given module")
    @GetMapping("/{module_name}")
    public ResponseEntity<List<String>> getModuleVersions(@PathVariable("module_name") final String moduleName) {

        log.debug("getModuleVersions moduleName: {}", moduleName);

        List<String> moduleVersions = moduleUseCases.getModuleVersions(moduleName);
        log.debug("return getModuleVersions: {}", moduleVersions.toString());

        return ResponseEntity.ok(moduleVersions);
    }

    @ApiOperation("Get all types for a given module version")
    @GetMapping("/{module_name}/{module_version:.+}")
    public ResponseEntity<List<String>> getModuleTypes(@PathVariable("module_name") final String moduleName,
                                                       @PathVariable("module_version") final String moduleVersion) {

        log.debug("getModuleTypes moduleName: {}, moduleVersion: {}", moduleName, moduleVersion);

        List<String> moduleTypes = moduleUseCases.getModuleTypes(moduleName, moduleVersion);
        log.debug("return getModuleTypes: {}", moduleTypes.toString());

        return ResponseEntity.ok(moduleTypes);
    }

    @ApiOperation("Get info for a given module release/working-copy")
    @GetMapping("/{module_name}/{module_version}/{module_type}")
    public ResponseEntity<ModuleIO> getModuleInfo(@PathVariable("module_name") final String moduleName,
                                                  @PathVariable("module_version") final String moduleVersion,
                                                  @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType) {

        log.debug("getModuleInfo moduleName: {}, moduleVersion: {}, moduleVersionType: {}", moduleName, moduleVersion, moduleVersionType);

        final TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        return moduleUseCases.getModule(moduleKey)
                .map(ModuleIO::new)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ModuleNotFoundException(moduleKey));
    }

    @ApiOperation("Delete a module")
    @DeleteMapping("/{module_name}/{module_version}/{module_type}")
    public ResponseEntity<Void> deleteModule(Authentication authentication,
                                             @PathVariable("module_name") final String moduleName,
                                             @PathVariable("module_version") final String moduleVersion,
                                             @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType) {

        log.info("deleteModule {} {}", moduleName, moduleVersion);

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        moduleUseCases.deleteModule(moduleKey, new User(authentication));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Create a release from an existing workingcopy")
    @PostMapping("/create_release")
    public ResponseEntity<ModuleIO> createRelease(Authentication authentication,
                                                  @RequestParam("module_name") final String moduleName,
                                                  @RequestParam("module_version") final String moduleVersion,
                                                  @RequestParam(value = "release_version", required = false) final String releaseVersion) {

        log.info("createRelease {} {} => {}", moduleName, moduleVersion, releaseVersion);

        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        ModuleView moduleView = moduleUseCases.createRelease(moduleName, moduleVersion, releaseVersion, new User(authentication));
        ModuleIO moduleOutput = new ModuleIO(moduleView);

        return ResponseEntity.ok(moduleOutput);
    }

    @ApiOperation("Deprecated - Use GET /modules/perform_search instead")
    @PostMapping("/perform_search")
    @Deprecated
    public ResponseEntity<List<ModuleIO>> postSearch(@ApiParam(value = "Format: name (+ version)", required = true)
                                                     @RequestParam final String terms) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-04-23\"")
                .header("Sunset", "Wed Apr 24 00:00:00 CEST 2020")
                .header("Link", "/modules/perform_search")
                .body(search(terms, 0).getBody());
    }

    @ApiOperation("Search for modules")
    @GetMapping("/perform_search")
    public ResponseEntity<List<ModuleIO>> search(@ApiParam(value = "Format: name (+ version)", required = true)
                                                 @RequestParam final String terms,
                                                 @RequestParam(required = false) final Integer size) {
        checkQueryParameterNotEmpty("terms", terms);

        List<ModuleView> moduleViews = moduleUseCases.search(terms, size);

        List<ModuleIO> moduleOutputs = Optional.ofNullable(moduleViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ModuleIO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(moduleOutputs);
    }

    @ApiOperation("Deprecated - Use GET /modules/search instead")
    @PostMapping("/search")
    @Deprecated
    public ResponseEntity<ModuleIO> postSearchSingle(@ApiParam(value = "Format: name (+ version) (+ true|false)", required = true)
                                                     @RequestParam final String terms) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-04-23\"")
                .header("Sunset", "Wed Apr 24 00:00:00 CEST 2020")
                .header("Link", "/modules/perform_search")
                .body(searchSingle(terms).getBody());
    }

    @ApiOperation("Search for a single module")
    @GetMapping("/search")
    public ResponseEntity<ModuleIO> searchSingle(@ApiParam(value = "Format: name (+ version) (+ true|false)", required = true)
                                                 @RequestParam final String terms) {

        return moduleUseCases.searchSingle(terms)
                .map(ModuleIO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiOperation("Get properties model")
    @GetMapping("/{module_name}/{module_version}/{module_type}/model")
    public ResponseEntity<ModelOutput> getModuleModel(@PathVariable("module_name") final String moduleName,
                                                      @PathVariable("module_version") final String moduleVersion,
                                                      @PathVariable("module_type") final TemplateContainer.VersionType versionType) {

        log.debug("getModuleModel {} {} {}", moduleName, moduleVersion, versionType);

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, versionType);
        List<AbstractPropertyView> abstractPropertyViews = moduleUseCases.getPropertiesModel(moduleKey);
        ModelOutput modelOutput = new ModelOutput(abstractPropertyViews);

        return ResponseEntity.ok(modelOutput);
    }

    @ApiOperation("Retrieve modules using techno")
    @GetMapping("/using_techno/{techno_name}/{techno_version}/{techno_type}")
    public ResponseEntity<List<ModuleKeyOutput>> getModulesUsingTechno(@PathVariable("techno_name") final String technoName,
                                                                       @PathVariable("techno_version") final String technoVersion,
                                                                       @PathVariable("techno_type") final TemplateContainer.VersionType technoVersionType) {

        Techno.Key technoKey = new Techno.Key(technoName, technoVersion, technoVersionType);
        List<TemplateContainerKeyView> moduleKeys = moduleUseCases.getModulesUsingTechno(technoKey);
        List<ModuleKeyOutput> moduleKeyOutputs = ModuleKeyOutput.fromViews(moduleKeys);

        return ResponseEntity.ok(moduleKeyOutputs);
    }
}
