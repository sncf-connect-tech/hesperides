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

import com.vsct.dt.hesperides.security.User;
import com.vsct.dt.hesperides.domain.modules.Module;
import com.vsct.dt.hesperides.domain.modules.ModuleSearchRepository;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Path("/toto")
@Api("/toto")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class ModuleApi {
    private final Executor executor = Executors.newFixedThreadPool(50);

    private ModuleSearchRepository moduleSearchRepository;

    @Inject
    public ModuleApi(final ModuleSearchRepository moduleSearchRepository) {
        this.moduleSearchRepository = moduleSearchRepository;
    }

    @GET
    //@Timed ?
    @ApiOperation("Get active module names")
    public void getActiveModulesName(@Auth final User user, @Suspended final AsyncResponse response) {
        // Essai de l'exécution asynchrone
        executor.execute(() -> {
            // Récupère la liste des noms de modules qui n'ont pas été supprimés
            // Le type Set permet d'évacuer les doublons
            Set<String> moduleNames = moduleSearchRepository.getModules().stream()
                    .map(Module::getName)
                    .collect(Collectors.toSet());
//            Set<String> moduleNames = new HashSet<>();
//            for (Module module : moduleSearchRepository.getModules()) {
//                moduleNames.add(module.getName());
//            }
            response.resume(moduleNames);
        });
    }
}
