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
import org.hesperides.application.technos.TechnoUseCases;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.inputs.TemplateInput;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public TechnoController(TechnoUseCases technoUseCases) {
        this.technoUseCases = technoUseCases;
    }

    @ApiOperation("Add a template to a techno working copy")
    @PostMapping(path = "/{techno_name}/{techno_version}/workingcopy/templates")
    public ResponseEntity<TemplateView> createWorkingCopy(Principal currentUser,
                                                          @PathVariable(value = "techno_name") final String technoName,
                                                          @PathVariable(value = "techno_version") final String technoVersion,
                                                          @Valid @RequestBody final TemplateInput templateInput) {
        log.info("Add a template to a techno working copy {} {}", technoName, technoVersion);

        TemplateContainer.Key technoKey = new TemplateContainer.Key(technoName, technoVersion, TemplateContainer.Type.workingcopy);
        technoUseCases.addTemplate(technoKey, templateInput.toDomainInstance(technoKey), fromPrincipal(currentUser));
        TemplateView templateView = technoUseCases.getTemplate(technoKey, templateInput.getName()).get();

        return ResponseEntity.status(HttpStatus.CREATED).body(templateView);
    }
}
