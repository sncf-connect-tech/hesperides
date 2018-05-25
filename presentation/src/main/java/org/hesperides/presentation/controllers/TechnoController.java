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
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.io.PartialTemplateIO;
import org.hesperides.presentation.io.TechnoIO;
import org.hesperides.presentation.io.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.domain.security.User.fromPrincipal;

@Slf4j
@Api("/templates/packages")
@RestController
@RequestMapping("/templates/packages/{techno_name}/{techno_version}/")
public class TechnoController extends BaseController {

    private final TechnoUseCases technoUseCases;

    @Autowired
    public TechnoController(TechnoUseCases technoUseCases) {
        this.technoUseCases = technoUseCases;
    }

    @ApiOperation("Add a template to a techno working copy")
    @PostMapping("workingcopy/templates")
    public ResponseEntity<TemplateIO> createWorkingCopy(Principal currentUser,
                                                        @PathVariable(value = "techno_name") final String technoName,
                                                        @PathVariable(value = "techno_version") final String technoVersion,
                                                        @Valid @RequestBody final TemplateIO templateInput) {

        log.info("Add a template to a techno working copy {} {}", technoName, technoVersion);

        TemplateContainer.Key technoKey = new TemplateContainer.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        technoUseCases.addTemplate(technoKey, templateInput.toDomainInstance(technoKey), fromPrincipal(currentUser));
        TemplateIO templateOutput = technoUseCases.getTemplate(technoKey, templateInput.getName())
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(technoKey, templateInput.getName()));

        return ResponseEntity.created(technoKey.getURI(Techno.KEY_PREFIX)).body(templateOutput);
    }

    @ApiOperation("Update a template")
    @PutMapping("workingcopy/templates")
    public ResponseEntity<TemplateIO> updateTemplateInWorkingCopy(Principal currentUser,
                                                                  @PathVariable("techno_name") final String technoName,
                                                                  @PathVariable("techno_version") final String technoVersion,
                                                                  @Valid @RequestBody final TemplateIO templateInput){

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        Template template = templateInput.toDomainInstance(technoKey);
        technoUseCases.updateTemplateInWorkingCopy(technoKey, template, fromPrincipal(currentUser));

        TemplateIO templateOutput = technoUseCases.getTemplate(technoKey, template.getName())
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(technoKey, template.getName()));

        return ResponseEntity.ok(templateOutput);

    }

    @ApiOperation("Delete a techno")
    @DeleteMapping("{version_type}")
    public ResponseEntity deleteTechno(Principal currentUser,
                                       @PathVariable("techno_name") final String technoName,
                                       @PathVariable("techno_version") final String technoVersion,
                                       @PathVariable("version_type") final TemplateContainer.VersionType versionType) {

        log.info("deleteTechno {} {} {}", technoName, technoVersion, versionType);

        TemplateContainer.Key technoKey = new TemplateContainer.Key(technoName, technoVersion, versionType);
        technoUseCases.deleteTechno(technoKey, fromPrincipal(currentUser));

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("workingcopy/templates/{template_name}")
    @ApiOperation("Delete template in the working copy of a version")
    public ResponseEntity deleteTemplateInWorkingCopy(Principal currentUser,
                                                      @PathVariable("techno_name") final String technoName,
                                                      @PathVariable("techno_version") final String technoVersion,
                                                      @PathVariable("template_name") final String templateName) {

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        this.technoUseCases.deleteTemplate(technoKey, templateName, fromPrincipal(currentUser));

        return ResponseEntity.noContent().build();
    }

    @ApiOperation("Get techno templates")
    @GetMapping("{version_type}/templates")
    public ResponseEntity<List<PartialTemplateIO>> getTemplates(@PathVariable("techno_name") final String technoName,
                                                                @PathVariable("techno_version") final String technoVersion,
                                                                @PathVariable("version_type") final TemplateContainer.VersionType versionType) {

        log.info("getTemplates {} {} {}", technoName, technoVersion, versionType);

        TemplateContainer.Key technoKey = new TemplateContainer.Key(technoName, technoVersion, versionType);
        List<TemplateView> templateViews = technoUseCases.getTemplates(technoKey);
        return ResponseEntity.ok(templateViews.stream().map(PartialTemplateIO::fromTemplateView).collect(Collectors.toList()));
    }

    @ApiOperation("Create a release from an existing workingcopy")
    @PostMapping("/create_release")
    public ResponseEntity<TechnoIO> releaseTechno(Principal currentUser,
                                                  @RequestParam("techno_name") final String technoName,
                                                  @RequestParam("techno_version") final String technoVersion) {

        log.info("releaseTechno {} {}", technoName, technoVersion);

        TemplateContainer.Key existingTechnoKey = new TemplateContainer.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        TechnoView technoView = technoUseCases.releaseTechno(existingTechnoKey, fromPrincipal(currentUser));
        TechnoIO technoOutput = TechnoIO.fromTechnoView(technoView);

        URI releasedTechnoLocation = technoView.toDomainInstance().getKey().getURI(Techno.KEY_PREFIX);
        return ResponseEntity.created(releasedTechnoLocation).body(technoOutput);
    }

    @ApiOperation("Search for technos")
    @PostMapping(path = "/perform_search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<TechnoIO>> search(@RequestParam("terms") final String input) {

        log.debug("search technos {}", input);

        List<TechnoView> technoViews = technoUseCases.search(input);
        List<TechnoIO> technoOutputs = technoViews != null
                ? technoViews.stream().map(TechnoIO::fromTechnoView).collect(Collectors.toList())
                : new ArrayList<>();

        return ResponseEntity.ok(technoOutputs);
    }
}
