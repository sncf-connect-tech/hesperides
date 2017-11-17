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
package com.vsct.dt.hesperides.api;

import com.vsct.dt.hesperides.domain.modules.Module;
import com.vsct.dt.hesperides.domain.modules.ModuleRepository;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/toto")
@Api("/toto")
//@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
//@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ModuleApi {

    private ModuleRepository moduleRepository;

    @Inject
    public ModuleApi(final ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @GET
    //@Timed ?
    @ApiOperation("Get active module names")
    public Collection<String> getActiveModulesName() {
        /**
         * Récupérer la liste des noms de modules qui n'ont pas été supprimés
         */
        List<String> moduleNames = new ArrayList<>();
        for (Module module : moduleRepository.getActiveModulesName()) {
            moduleNames.add(module.getName());
        }
        return moduleNames;
    }
}
