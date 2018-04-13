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
import org.hesperides.application.TechnoUseCases;
import org.hesperides.domain.technos.entities.Techno;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

import static org.hesperides.domain.security.User.fromPrincipal;

@Slf4j
@Api("/templates/packages")
@RestController
@RequestMapping("/templates/packages")
public class TechnoController extends BaseController {

    private final TechnoUseCases technoUseCases;

    public TechnoController(TechnoUseCases technoUseCases) {
        this.technoUseCases = technoUseCases;
    }

    @ApiOperation("Create a working copy")
    @PostMapping(path = "/{techno_name}/{techno_version}/workingcopy/templates")
    public ResponseEntity createWorkingCopy(Principal currentUser,
                                            @PathVariable(value = "techno_name") final String technoName,
                                            @PathVariable(value = "techno_version") final String technoVersion,
                                            @Valid @RequestBody final TechnoInput techno) {
        log.info("createWorkingCopy {}", techno.toString());

        Techno.Key createdTechnoKey = technoUseCases.createWorkingCopy(techno.toDomainInstance(technoVersion, true), fromPrincipal(currentUser));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdTechnoKey);
    }
}
