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
import org.hesperides.application.Modules;
import org.hesperides.domain.modules.ModuleType;
import org.hesperides.domain.modules.commands.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.presentation.exceptions.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

import static org.hesperides.domain.modules.ModuleType.release;
import static org.hesperides.domain.modules.ModuleType.workingcopy;
import static org.springframework.http.HttpStatus.SEE_OTHER;

@Api("/modules")
@RestController
@RequestMapping("/modules")
public class ModuleController extends BaseResource {

    private final Modules modules;

    public ModuleController(Modules modules) {
        this.modules = modules;
    }

    @ApiOperation("Get all module names")
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Collection<String> getModulesNames() {
        return modules.getModulesNames();
    }

    @ApiOperation("Get info for a given module release/working-copy")
    @GetMapping("/{module_name}/{module_version}/{module_type}")
    public ResponseEntity<ModuleView> getModuleInfo(
                                @PathVariable("module_name") final String moduleName,
                                @PathVariable("module_version") final String moduleVersion,
                                @PathVariable("module_type") final ModuleType moduleType
    ) {
        final Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleType);
        return modules.getModule(moduleKey).map(ResponseEntity::ok).orElseThrow(() -> new NotFoundException("Could not find module info for " + moduleKey));
    }

    @ApiOperation("Create a working copy (possibly from a release)")
    @PostMapping
    public ResponseEntity createWorkingCopy(@RequestParam(value = "from_module_name", required = false) final String from_module_name,
                                      @RequestParam(value = "from_module_version", required = false) final String from_module_version,
                                      @RequestParam(value = "from_is_working_copy",required = false) final Boolean isFromWorkingCopy,
                                      @Valid @RequestBody final org.hesperides.presentation.legacydtos.Module module) {

        if ((from_module_name == null || StringUtils.isBlank(from_module_name))
                && (from_module_version == null || StringUtils.isBlank(from_module_version))
                && isFromWorkingCopy == null) {

            Module.Key created = modules.createWorkingCopy(module.getName(), module.getVersion());

            return ResponseEntity.status(SEE_OTHER).location(created.getURI()).build();

        } else {
            checkQueryParameterNotEmpty("from_module_name", from_module_name);
            checkQueryParameterNotEmpty("from_module_version", from_module_version);
            checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

            Module.Key from = new Module.Key(from_module_name, from_module_version, isFromWorkingCopy ? workingcopy: release);

            Module.Key created = modules.createWorkingCopyFrom(module.getName(), module.getVersion(), from);

            return ResponseEntity.status(SEE_OTHER).location(created.getURI()).build();
        }
    }
}
