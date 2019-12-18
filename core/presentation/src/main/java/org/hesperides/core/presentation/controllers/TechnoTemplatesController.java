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
package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.technos.TechnoUseCases;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Api(tags = "08. Techno templates", description = " ")
@RequestMapping({"/templates/packages", "/technos"})
@RestController
public class TechnoTemplatesController extends AbstractController {

    private final TechnoUseCases technoUseCases;

    @Autowired
    public TechnoTemplatesController(TechnoUseCases technoUseCases) {
        this.technoUseCases = technoUseCases;
    }

    @ApiOperation("Update a template")
    @PutMapping("/{techno_name}/{techno_version}/workingcopy/templates")
    public ResponseEntity<TemplateIO> updateTemplateInWorkingCopy(Authentication authentication,
                                                                  @PathVariable("techno_name") final String technoName,
                                                                  @PathVariable("techno_version") final String technoVersion,
                                                                  @Valid @RequestBody final TemplateIO templateInput) {

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        Template template = templateInput.toDomainInstance(technoKey);
        technoUseCases.updateTemplateInWorkingCopy(technoKey, template, new User(authentication));

        TemplateIO templateOutput = technoUseCases.getTemplate(technoKey, template.getName())
                .map(TemplateIO::new)
                .orElseThrow(() -> new TemplateNotFoundException(technoKey, template.getName()));

        return ResponseEntity.ok(templateOutput);

    }

    @GetMapping("/{techno_name}/{techno_version}/{techno_type}/templates/{template_name:.+}")
    @ApiOperation("Get template's detail")
    public ResponseEntity<TemplateIO> getTemplate(@PathVariable("techno_name") final String technoName,
                                                  @PathVariable("techno_version") final String technoVersion,
                                                  @PathVariable("techno_type") final TemplateContainer.VersionType technoVersionType,
                                                  @PathVariable("template_name") final String templateName) {

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, technoVersionType);
        TemplateIO templateOutput = technoUseCases.getTemplate(technoKey, templateName)
                .map(TemplateIO::new)
                .orElseThrow(() -> new TemplateNotFoundException(technoKey, templateName));
        return ResponseEntity.ok(templateOutput);
    }

    @ApiOperation("Delete template in the working copy of a version")
    @DeleteMapping("/{techno_name}/{techno_version}/workingcopy/templates/{template_name:.+}")
    public ResponseEntity<Void> deleteTemplateInWorkingCopy(Authentication authentication,
                                                            @PathVariable("techno_name") final String technoName,
                                                            @PathVariable("techno_version") final String technoVersion,
                                                            @PathVariable("template_name") final String templateName) {

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        this.technoUseCases.deleteTemplate(technoKey, templateName, new User(authentication));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get techno templates")
    @GetMapping("/{techno_name}/{techno_version}/{version_type}/templates")
    public ResponseEntity<List<PartialTemplateIO>> getTemplates(@PathVariable("techno_name") final String technoName,
                                                                @PathVariable("techno_version") final String technoVersion,
                                                                @PathVariable("version_type") final TemplateContainer.VersionType versionType) {

        log.info("getTemplates {} {} {}", technoName, technoVersion, versionType);

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, versionType);
        List<TemplateView> templateViews = technoUseCases.getTemplates(technoKey);
        return ResponseEntity.ok(templateViews.stream().map(PartialTemplateIO::new).collect(Collectors.toList()));
    }
}
