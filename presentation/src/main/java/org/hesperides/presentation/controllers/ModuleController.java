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
package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.application.modules.ModuleUseCases;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.presentation.io.ModuleIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.domain.security.User.fromPrincipal;

@Slf4j
@Api("/modules")
@RestController
@RequestMapping("/modules")
@CrossOrigin
public class ModuleController extends BaseController {

    private final ModuleUseCases moduleUseCases;

    @Autowired
    public ModuleController(ModuleUseCases moduleUseCases) {
        this.moduleUseCases = moduleUseCases;
    }

    @ApiOperation("Create a working copy (possibly from a release)")
    @PostMapping
    public ResponseEntity<ModuleIO> createWorkingCopy(Principal currentUser,
                                                      @RequestParam(value = "from_module_name", required = false) final String fromModuleName,
                                                      @RequestParam(value = "from_module_version", required = false) final String fromModuleVersion,
                                                      @RequestParam(value = "from_is_working_copy", required = false) final Boolean fromWorkingCopy,
                                                      @Valid @RequestBody final ModuleIO moduleInput) {

        log.info("createWorkingCopy {}", moduleInput.toString());

        ResponseEntity response;
        if (StringUtils.isBlank(fromModuleName)
                && StringUtils.isBlank(fromModuleVersion)
                && fromWorkingCopy == null) {

            Module.Key createdModuleKey = moduleUseCases.createWorkingCopy(moduleInput.toDomainInstance(), fromPrincipal(currentUser));
            ModuleIO moduleOutput = moduleUseCases.getModule(createdModuleKey)
                    .map(ModuleIO::fromModuleView)
                    .orElseThrow(() -> new ModuleNotFoundException(createdModuleKey));
            response = ResponseEntity.created(createdModuleKey.getURI(Module.KEY_PREFIX)).body(moduleOutput);

        } else {
            checkQueryParameterNotEmpty("from_module_name", fromModuleName);
            checkQueryParameterNotEmpty("from_module_version", fromModuleVersion);
            checkQueryParameterNotEmpty("from_is_working_copy", fromWorkingCopy);

            Module.Key existingModuleKey = new Module.Key(fromModuleName, fromModuleVersion, TemplateContainer.getVersionType(fromWorkingCopy));
            ModuleView moduleView = moduleUseCases.createWorkingCopyFrom(existingModuleKey, moduleInput.toDomainInstance().getKey(), fromPrincipal(currentUser));
            TemplateContainer.Key createdModuleKey = moduleView.toDomainInstance().getKey();
            ModuleIO moduleOutput = ModuleIO.fromModuleView(moduleView);
            response = ResponseEntity.created(createdModuleKey.getURI(Module.KEY_PREFIX)).body(moduleOutput);
        }
        return response;
    }

    @ApiOperation("Update a module working copy")
    @PutMapping
    public ResponseEntity<ModuleIO> updateWorkingCopy(Principal currentUser, @Valid @RequestBody final ModuleIO moduleInput) {

        log.info("Updating module workingcopy {}", moduleInput.toString());

        Module module = moduleInput.toDomainInstance();
        moduleUseCases.updateModuleTechnos(module, fromPrincipal(currentUser));
        ModuleIO moduleOutput = moduleUseCases.getModule(module.getKey())
                .map(ModuleIO::fromModuleView)
                .orElseThrow(() -> new ModuleNotFoundException(module.getKey()));

        return ResponseEntity.ok(moduleOutput);
    }

    @ApiOperation("Get all module names")
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<String>> getModulesNames() {

        log.debug("getModulesNames");

        List<String> modulesNames = moduleUseCases.getModulesNames();
        log.debug("return getModulesNames: {}", modulesNames.toString());

        return ResponseEntity.ok(modulesNames);
    }

    @ApiOperation("Get all versions for a given module")
    @GetMapping(path = "/{module_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<String>> getModuleVersions(@PathVariable("module_name") final String moduleName) {

        log.debug("getModuleVersions moduleName: {}", moduleName);

        List<String> moduleVersions = moduleUseCases.getModuleVersions(moduleName);
        log.debug("return getModuleVersions: {}", moduleVersions.toString());

        return ResponseEntity.ok(moduleVersions);
    }

    @ApiOperation("Get all types for a given module version")
    @GetMapping(path = "/{module_name}/{module_version:.+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<String>> getModuleTypes(@PathVariable("module_name") final String moduleName,
                                                       @PathVariable("module_version") final String moduleVersion) {

        log.debug("getModuleTypes moduleName: {}, moduleVersion: {}", moduleName, moduleVersion);

        List<String> moduleTypes = moduleUseCases.getModuleTypes(moduleName, moduleVersion);
        log.debug("return getModuleTypes: {}", moduleTypes.toString());

        return ResponseEntity.ok(moduleTypes);
    }

    @ApiOperation("Get info for a given module release/working-copy")
    @GetMapping(path = "/{module_name}/{module_version}/{module_type}")
    public ResponseEntity<ModuleIO> getModuleInfo(@PathVariable("module_name") final String moduleName,
                                                  @PathVariable("module_version") final String moduleVersion,
                                                  @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType) {

        log.debug("getModuleInfo moduleName: {}, moduleVersion: {}, moduleVersionType: {}", moduleName, moduleVersion, moduleVersionType);

        final Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        return moduleUseCases.getModule(moduleKey)
                .map(ModuleIO::fromModuleView)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ModuleNotFoundException(moduleKey));
    }

    @ApiOperation("Delete a module")
    @DeleteMapping(path = "/{module_name}/{module_version}/{module_type}")
    public ResponseEntity deleteModule(Principal currentUser,
                                       @PathVariable("module_name") final String moduleName,
                                       @PathVariable("module_version") final String moduleVersion,
                                       @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType) {

        log.info("deleteModule {} {}", moduleName, moduleVersion);

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        moduleUseCases.deleteModule(moduleKey, fromPrincipal(currentUser));

        return ResponseEntity.ok().build(); // Should be ResponseEntity.accepted()
    }

    @ApiOperation(("Create a release from an existing workingcopy"))
    @PostMapping(path = "/create_release")
    public ResponseEntity<ModuleIO> createRelease(Principal currentUser,
                                                  @RequestParam("module_name") final String moduleName,
                                                  @RequestParam("module_version") final String moduleVersion,
                                                  @RequestParam(value = "release_version", required = false) final String releaseVersion) {

        log.info("createRelease {} {} => {}", moduleName, moduleVersion, releaseVersion);

        ModuleView moduleView = moduleUseCases.createRelease(moduleName, moduleVersion, releaseVersion, fromPrincipal(currentUser));
        ModuleIO moduleOutput = ModuleIO.fromModuleView(moduleView);

        return ResponseEntity.ok(moduleOutput);
    }

    @ApiOperation("Search for modules")
    @PostMapping(path = "/perform_search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<ModuleIO>> search(@RequestParam("terms") final String input) {

        log.debug("search module {}", input);

        List<ModuleView> moduleViews = moduleUseCases.search(input);
        List<ModuleIO> moduleOutputs = moduleViews != null
                ? moduleViews.stream().map(ModuleIO::fromModuleView).collect(Collectors.toList())
                : new ArrayList<>();

        return ResponseEntity.ok(moduleOutputs);
    }
}
