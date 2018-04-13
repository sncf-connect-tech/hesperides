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
import org.hesperides.application.TechnoUseCases;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.exceptions.TechnoNotFoundException;
import org.hesperides.domain.technos.queries.TechnoView;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.hesperides.domain.security.User.fromPrincipal;
import static org.springframework.http.HttpStatus.SEE_OTHER;

@Slf4j
@Api("/technos")
@RestController
@RequestMapping("/technos")
public class TechnoController extends BaseController {

    private final TechnoUseCases technoUseCases;

    public TechnoController(TechnoUseCases technoUseCases) {
        this.technoUseCases = technoUseCases;
    }

    @ApiOperation("Create a working copy")
    @PostMapping
    public ResponseEntity createWorkingCopy(Principal currentUser, @Valid @RequestBody final TechnoInput techno) {
        log.info("createWorkingCopy {}", techno.toString());
        return null;
    }
}
