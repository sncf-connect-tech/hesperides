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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.application.modules.ModuleUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.security.User;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api("/modules")
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

        ResponseEntity response;
        User currentUser = fromAuthentication(authentication);
        if (StringUtils.isBlank(fromModuleName)
                && StringUtils.isBlank(fromModuleVersion)
                && isFromWorkingCopy == null) {

            TemplateContainer.Key createdModuleKey = moduleUseCases.createWorkingCopy(moduleInput.toDomainInstance(), currentUser);
            ModuleIO moduleOutput = moduleUseCases.getModule(createdModuleKey)
                    .map(ModuleIO::new)
                    .orElseThrow(() -> new ModuleNotFoundException(createdModuleKey));
            response = ResponseEntity.created(createdModuleKey.getURI()).body(moduleOutput);

        } else {
            checkQueryParameterNotEmpty("from_module_name", fromModuleName);
            checkQueryParameterNotEmpty("from_module_version", fromModuleVersion);
            checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

            TemplateContainer.Key existingModuleKey = new Module.Key(fromModuleName, fromModuleVersion, TemplateContainer.getVersionType(isFromWorkingCopy));
            ModuleView moduleView = moduleUseCases.createWorkingCopyFrom(existingModuleKey, moduleInput.toDomainInstance().getKey(), currentUser);
            TemplateContainer.Key createdModuleKey = moduleView.toDomainInstance().getKey();
            ModuleIO moduleOutput = new ModuleIO(moduleView);
            response = ResponseEntity.created(createdModuleKey.getURI()).body(moduleOutput);
        }
        return response;
    }

    @ApiOperation("Update a module working copy")
    @PutMapping
    public ResponseEntity<ModuleIO> updateWorkingCopy(Authentication authentication, @Valid @RequestBody final ModuleIO moduleInput) {

        log.info("Updating module workingcopy {}", moduleInput.toString());

        Module module = moduleInput.toDomainInstance();
        moduleUseCases.updateModuleTechnos(module, fromAuthentication(authentication));
        ModuleIO moduleOutput = moduleUseCases.getModule(module.getKey())
                .map(ModuleIO::new)
                .orElseThrow(() -> new ModuleNotFoundException(module.getKey()));

        return ResponseEntity.ok(moduleOutput);
    }

    @ApiOperation("Get all module names")
    @GetMapping
    public ResponseEntity<List<String>> getModulesNames() {

        log.debug("getModulesNames");

        List<String> modulesNames = moduleUseCases.getModulesNames();
        log.debug("return getModulesNames: {}", modulesNames.toString());

        return ResponseEntity.ok(modulesNames);
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
    public ResponseEntity deleteModule(Authentication authentication,
                                       @PathVariable("module_name") final String moduleName,
                                       @PathVariable("module_version") final String moduleVersion,
                                       @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType) {

        log.info("deleteModule {} {}", moduleName, moduleVersion);

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        moduleUseCases.deleteModule(moduleKey, fromAuthentication(authentication));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Create a release from an existing workingcopy")
    @PostMapping("/create_release")
    public ResponseEntity<ModuleIO> createRelease(Authentication authentication,
                                                  @RequestParam("module_name") final String moduleName,
                                                  @RequestParam("module_version") final String moduleVersion,
                                                  @RequestParam(value = "release_version", required = false) final String releaseVersion) {

        log.info("createRelease {} {} => {}", moduleName, moduleVersion, releaseVersion);

        ModuleView moduleView = moduleUseCases.createRelease(moduleName, moduleVersion, releaseVersion, fromAuthentication(authentication));
        ModuleIO moduleOutput = new ModuleIO(moduleView);

        return ResponseEntity.ok(moduleOutput);
    }

    @ApiOperation("Search for modules")
    @PostMapping("/perform_search")
    public ResponseEntity<List<ModuleIO>> search(@RequestParam("terms") final String input) {

        log.debug("search module {}", input);

        List<ModuleView> moduleViews = moduleUseCases.search(input);
        List<ModuleIO> moduleOutputs = Optional.ofNullable(moduleViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(ModuleIO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(moduleOutputs);
    }

    @ApiOperation("Get properties model")
    @GetMapping("/{module_name}/{module_version}/{module_type}/model")
    public ResponseEntity<ModelOutput> getModuleModel(@PathVariable("module_name") final String moduleName,
                                                      @PathVariable("module_version") final String moduleVersion,
                                                      @PathVariable("module_type") final TemplateContainer.VersionType versionType) {

        log.debug("getModuleModel {} {} {}", moduleName, moduleVersion, versionType);

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, versionType);
        List<AbstractPropertyView> abstractPropertyViews = moduleUseCases.getProperties(moduleKey);
        ModelOutput modelOutput = new ModelOutput(abstractPropertyViews);

        return ResponseEntity.ok(modelOutput);
    }
}