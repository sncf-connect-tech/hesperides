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
import org.hesperides.presentation.inputs.ModuleInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.hesperides.domain.security.User.fromPrincipal;
import static org.springframework.http.HttpStatus.SEE_OTHER;

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

    @ApiOperation("Get all module names")
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> getModulesNames() {
        log.debug("getModulesNames");
        List<String> modulesNames = moduleUseCases.getModulesNames();
        log.debug("return getModulesNames: {}", modulesNames.toString());
        return modulesNames;
    }

    @ApiOperation("Get info for a given module release/working-copy")
    @GetMapping(path = "/{module_name}/{module_version}/{module_type}")
    public ResponseEntity<ModuleView> getModuleInfo(
            @PathVariable("module_name") final String moduleName,
            @PathVariable("module_version") final String moduleVersion,
            @PathVariable("module_type") final Module.Type moduleType) {
        log.debug("getModuleInfo moduleName: {}, moduleVersion: {}, moduleType: {}", moduleName, moduleVersion, moduleType);
        final Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleType);
        ResponseEntity<ModuleView> module = moduleUseCases.getModule(moduleKey).map(ResponseEntity::ok).orElseThrow(() -> new ModuleNotFoundException(moduleKey));
        log.debug("return getModuleInfo: {}", module.getBody().toString());
        return module;
    }

    @ApiOperation("Get all versions for a given module")
    @GetMapping(path = "/{module_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> getModuleVersions(@PathVariable("module_name") final String moduleName) {
        log.debug("getModuleVersions moduleName: {}", moduleName);
        List<String> moduleVersions = moduleUseCases.getModuleVersions(moduleName);
        log.debug("return getModuleVersions: {}", moduleVersions.toString());
        return moduleVersions;
    }

    @ApiOperation("Get all types for a given module version")
    @GetMapping(path = "/{module_name}/{module_version:.+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<String> getModuleTypes(@PathVariable("module_name") final String moduleName,
                                       @PathVariable("module_version") final String moduleVersion) {
        log.debug("getModuleTypes moduleName: {}, moduleVersion: {}", moduleName, moduleVersion);
        List<String> moduleTypes = moduleUseCases.getModuleTypes(moduleName, moduleVersion);
        log.debug("return getModuleTypes: {}", moduleTypes.toString());
        return moduleTypes;
    }

    @ApiOperation("Create a working copy (possibly from a release)")
    @PostMapping
    public ResponseEntity createWorkingCopy(Principal currentUser,
                                            @RequestParam(value = "from_module_name", required = false) final String fromModuleName,
                                            @RequestParam(value = "from_module_version", required = false) final String fromModuleVersion,
                                            @RequestParam(value = "from_is_working_copy", required = false) final Boolean isFromWorkingCopy,
                                            @Valid @RequestBody final ModuleInput module) {

        log.info("createWorkingCopy {}", module.toString());

        Module.Key createdModuleKey;
        if (StringUtils.isBlank(fromModuleName)
                && StringUtils.isBlank(fromModuleVersion)
                && isFromWorkingCopy == null) {

            createdModuleKey = moduleUseCases.createWorkingCopy(module.toDomainInstance(), fromPrincipal(currentUser));

        } else {
            checkQueryParameterNotEmpty("from_module_name", fromModuleName);
            checkQueryParameterNotEmpty("from_module_version", fromModuleVersion);
            checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

            Module.Key from = new Module.Key(fromModuleName, fromModuleVersion, isFromWorkingCopy ? Module.Type.workingcopy : Module.Type.release);
            createdModuleKey = moduleUseCases.createWorkingCopyFrom(from, module.getDomaineModuleKey());
        }

        return ResponseEntity.status(SEE_OTHER).location(createdModuleKey.getURI()).build();
    }

    @ApiOperation("Update a module working copy")
    @PutMapping
    public ResponseEntity updateWorkingCopy(Principal principal,
                                            @Valid @RequestBody final ModuleInput module) {
        log.info("Updating module workingcopy {}", module.toString());
        Module.Key updated = moduleUseCases.updateWorkingCopy(module.toDomainInstance(), fromPrincipal(principal));
        return ResponseEntity.status(SEE_OTHER).location(updated.getURI()).build();
    }

    @ApiOperation("Deletes the working copy")
    @DeleteMapping(path = "/{module_name}/{module_version}/workingcopy")
    public ResponseEntity deleteWorkingCopy(Principal currentUser,
                                            @PathVariable(value = "module_name") final String moduleName,
                                            @PathVariable(value = "module_version") final String moduleVersion) {

        log.info("deleteWorkingCopy {} {}", moduleName, moduleVersion);

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, Module.Type.workingcopy);
        Module module = new Module(moduleKey, Collections.emptyList(), Collections.emptyList(), Long.MIN_VALUE);

        moduleUseCases.deleteWorkingCopy(module, fromPrincipal(currentUser));

        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Deletes the release")
    @DeleteMapping(path = "/{module_name}/{module_version}/release")
    public ResponseEntity deleteRelease(Principal currentUser,
                                        @PathVariable(value = "module_name") final String moduleName,
                                        @PathVariable(value = "module_version") final String moduleVersion) {

        log.info("deleteRelease {} {}", moduleName, moduleVersion);

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, Module.Type.release);
        Module module = new Module(moduleKey, Collections.emptyList(), Collections.emptyList(), Long.MIN_VALUE);

        // TODO N'envoyer que la clé
        moduleUseCases.deleteRelease(module, fromPrincipal(currentUser));

        // TODO Pourquoi vérifier après la suppression ?
        checkQueryParameterNotEmpty("module_name", moduleName);
        checkQueryParameterNotEmpty("module_version", moduleVersion);

        return ResponseEntity.ok().build();
    }
}
