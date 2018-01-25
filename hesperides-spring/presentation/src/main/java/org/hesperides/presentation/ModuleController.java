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
package org.hesperides.presentation;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.application.Modules;
import org.hesperides.domain.Module;
import org.hesperides.domain.ModuleSearchRepository;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("/modules")
@Api("/modules")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Service
public class ModuleController extends BaseResource {

    private final ModuleSearchRepository moduleSearchRepository;

    private final Modules modules;

    public ModuleController(ModuleSearchRepository moduleSearchRepository, Modules modules) {
        this.moduleSearchRepository = moduleSearchRepository;
        this.modules = modules;
    }

    @GET
    public Collection<String> getModules() {
        return moduleSearchRepository.getModules().stream()
                .map(Module::getName)
                .collect(Collectors.toSet());
    }


    @POST
    @Timed
    @ApiOperation("Create a working copy (possibly from a release)")
    public Response createWorkingCopy(@Context final SecurityContext user,
                                      @QueryParam("from_module_name") final String from_module_name,
                                      @QueryParam("from_module_version") final String from_module_version,
                                      @QueryParam("from_is_working_copy") final Boolean isFromWorkingCopy,
                                      @Valid final org.hesperides.presentation.legacydtos.Module module) {

        if ((from_module_name == null || StringUtils.isBlank(from_module_name))
                && (from_module_version == null || StringUtils.isBlank(from_module_version))
                && isFromWorkingCopy == null) {

            Module created = modules.createWorkingCopy(module.getName(), module.getVersion());

            return Response.status(Response.Status.CREATED).entity(created).build();

        } else {
            checkQueryParameterNotEmpty("from_module_name", from_module_name);
            checkQueryParameterNotEmpty("from_module_version", from_module_version);
            checkQueryParameterNotEmpty("from_is_working_copy", isFromWorkingCopy);

            Module created = modules.createWorkingCopyFrom(module.getName(), module.getVersion(), from_module_name, from_module_version);

            return Response.status(Response.Status.CREATED).entity(created).build();
        }
    }
}
