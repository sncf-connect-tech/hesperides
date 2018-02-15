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
import org.apache.commons.lang3.StringUtils;
import org.hesperides.application.ModuleUseCases;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.ModuleNotFoundException;
import org.hesperides.domain.modules.queries.ModuleView;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.SEE_OTHER;

@Api("/modules")
@RestController
@RequestMapping("/modules")
public class ModuleController extends BaseResource {

    private final ModuleUseCases moduleUseCases;

    public ModuleController(ModuleUseCases moduleUseCases) {
        this.moduleUseCases = moduleUseCases;
    }

    @ApiOperation("Get all module names")
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CompletableFuture<List<String>> getModulesNames() {
        return moduleUseCases.getModulesNames();
    }

    @ApiOperation("Get info for a given module release/working-copy")
    @GetMapping("/{module_name}/{module_version}/{module_type}")
    public CompletableFuture<ResponseEntity<ModuleView>> getModuleInfo(
            @PathVariable("module_name") final String moduleName,
            @PathVariable("module_version") final String moduleVersion,
            @PathVariable("module_type") final Module.Type moduleType) {

        final Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleType);
        return moduleUseCases.getModule(moduleKey).thenApply(optionalView -> optionalView.map(ResponseEntity::ok).orElseThrow(() -> new ModuleNotFoundException(moduleKey)));
    }

    @ApiOperation("Create a working copy (possibly from a release)")
    @PostMapping
    public ResponseEntity createWorkingCopy(@RequestParam(value = "from_module_name", required = false) final String fromModuleName,
                                            @RequestParam(value = "from_module_version", required = false) final String fromModuleVersion,
                                            @RequestParam(value = "from_is_working_copy", required = false) final Boolean isFromWorkingCopy,
                                            @Valid @RequestBody final ModuleInput module) {

        if ((fromModuleName == null || StringUtils.isBlank(fromModuleName))
                && (fromModuleVersion == null || StringUtils.isBlank(fromModuleVersion))
                && isFromWorkingCopy == null) {

            Module.Key created = moduleUseCases.createWorkingCopy(module.getKey());
            return ResponseEntity.status(SEE_OTHER).location(created.getURI()).build();

        } else {
            checkQueryParameterNotEmpty("from_module_name", fromModuleName);
            checkQueryParameterNotEmpty("from_module_version", fromModuleVersion);
            checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

            Module.Key from = new Module.Key(fromModuleName, fromModuleVersion, isFromWorkingCopy ? Module.Type.workingcopy : Module.Type.release);
            Module.Key created = moduleUseCases.createWorkingCopyFrom(from, module.getKey());
            return ResponseEntity.status(SEE_OTHER).location(created.getURI()).build();
        }
    }
}
