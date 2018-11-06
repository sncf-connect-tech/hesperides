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
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.exception.TechnoNotFoundException;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.security.User.fromAuthentication;

@Slf4j
@Api("/templates/packages")
@RequestMapping("/templates/packages")
@RestController
public class TechnosController extends AbstractController {

    private final TechnoUseCases technoUseCases;

    @Autowired
    public TechnosController(TechnoUseCases technoUseCases) {
        this.technoUseCases = technoUseCases;
    }

    @ApiOperation("Add a template to a techno working copy")
    @PostMapping("/{techno_name}/{techno_version}/workingcopy/templates")
    public ResponseEntity<TemplateIO> createWorkingCopy(Authentication authentication,
                                                        @PathVariable("techno_name") final String technoName,
                                                        @PathVariable("techno_version") final String technoVersion,
                                                        @Valid @RequestBody final TemplateIO templateInput) {

        log.info("Add a template to a techno working copy {} {}", technoName, technoVersion);

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        technoUseCases.addTemplate(technoKey, templateInput.toDomainInstance(technoKey), fromAuthentication(authentication));
        TemplateIO templateOutput = technoUseCases.getTemplate(technoKey, templateInput.getName())
                .map(TemplateIO::new)
                .orElseThrow(() -> new TemplateNotFoundException(technoKey, templateInput.getName()));

        return ResponseEntity.created(technoKey.getURI()).body(templateOutput);
    }

    @ApiOperation("Get info for a given techno release/working-copy")
    @GetMapping("/{techno_name}/{techno_version}/{techno_type}")
    public ResponseEntity<TechnoIO> getTechnoInfo(@PathVariable("techno_name") final String technoName,
                                                  @PathVariable("techno_version") final String technoVersion,
                                                  @PathVariable("techno_type") final TemplateContainer.VersionType technoVersionType) {

        log.debug("getTechnoInfo technoName: {}, technoVersion: {}, technoVersionType: {}", technoName, technoVersion, technoVersionType);

        final TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, technoVersionType);
        return technoUseCases.getTechno(technoKey)
                .map(TechnoIO::new)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new TechnoNotFoundException(technoKey));
    }

    @ApiOperation("Update a template")
    @PutMapping("/{techno_name}/{techno_version}/workingcopy/templates")
    public ResponseEntity<TemplateIO> updateTemplateInWorkingCopy(Authentication authentication,
                                                                  @PathVariable("techno_name") final String technoName,
                                                                  @PathVariable("techno_version") final String technoVersion,
                                                                  @Valid @RequestBody final TemplateIO templateInput) {

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        Template template = templateInput.toDomainInstance(technoKey);
        technoUseCases.updateTemplateInWorkingCopy(technoKey, template, fromAuthentication(authentication));

        TemplateIO templateOutput = technoUseCases.getTemplate(technoKey, template.getName())
                .map(TemplateIO::new)
                .orElseThrow(() -> new TemplateNotFoundException(technoKey, template.getName()));

        return ResponseEntity.ok(templateOutput);

    }

    @GetMapping("/{techno_name}/{techno_version}/{techno_type}/templates/{template_name:.+}")
    @ApiOperation("Get template's details")
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

    @ApiOperation("Delete a techno")
    @DeleteMapping("/{techno_name}/{techno_version}/{version_type}")
    public ResponseEntity deleteTechno(Authentication authentication,
                                       @PathVariable("techno_name") final String technoName,
                                       @PathVariable("techno_version") final String technoVersion,
                                       @PathVariable("version_type") final TemplateContainer.VersionType versionType) {

        log.info("deleteTechno {} {} {}", technoName, technoVersion, versionType);

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, versionType);
        technoUseCases.deleteTechno(technoKey, fromAuthentication(authentication));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Delete template in the working copy of a version")
    @DeleteMapping("/{techno_name}/{techno_version}/workingcopy/templates/{template_name:.+}")
    public ResponseEntity deleteTemplateInWorkingCopy(Authentication authentication,
                                                      @PathVariable("techno_name") final String technoName,
                                                      @PathVariable("techno_version") final String technoVersion,
                                                      @PathVariable("template_name") final String templateName) {

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        this.technoUseCases.deleteTemplate(technoKey, templateName, fromAuthentication(authentication));

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

    @ApiOperation("Create a release from an existing workingcopy")
    @PostMapping("/create_release")
    public ResponseEntity<TechnoIO> releaseTechno(Authentication authentication,
                                                  @RequestParam("techno_name") final String technoName,
                                                  @RequestParam("techno_version") final String technoVersion) {

        log.info("releaseTechno {} {}", technoName, technoVersion);

        TemplateContainer.Key existingTechnoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        TechnoView technoView = technoUseCases.releaseTechno(existingTechnoKey, fromAuthentication(authentication));
        TechnoIO technoOutput = new TechnoIO(technoView);

        URI releasedTechnoLocation = technoView.toDomainInstance().getKey().getURI();
        return ResponseEntity.created(releasedTechnoLocation).body(technoOutput);
    }

    @ApiOperation("Search for technos")
    @PostMapping("/perform_search")
    public ResponseEntity<List<TechnoIO>> search(@RequestParam("terms") final String input) {

        log.debug("search technos {}", input);

        List<TechnoView> technoViews = technoUseCases.search(input);
        List<TechnoIO> technoOutputs = Optional.ofNullable(technoViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(TechnoIO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(technoOutputs);
    }

    @ApiOperation("Create a copy of a techno")
    @PostMapping
    public ResponseEntity<TechnoIO> copyTechno(Authentication authentication,
                                               @RequestParam("from_package_name") final String fromTechnoName,
                                               @RequestParam("from_package_version") final String fromTechnoVersion,
                                               @RequestParam("from_is_working_copy") final Boolean isFromWorkingCopy,
                                               @Valid @RequestBody final TechnoIO technoInput) {

        log.info("copyTechno {}", technoInput.toString());

        TemplateContainer.Key existingTechnoKey = new Techno.Key(fromTechnoName, fromTechnoVersion, TemplateContainer.getVersionType(isFromWorkingCopy));
        TemplateContainer.Key newTechnoKey = new Techno.Key(technoInput.getName(), technoInput.getVersion(), TemplateContainer.VersionType.workingcopy);
        TechnoView technoView = technoUseCases.createWorkingCopyFrom(existingTechnoKey, newTechnoKey, fromAuthentication(authentication));
        TemplateContainer.Key createdTechnoKey = technoView.toDomainInstance().getKey();
        TechnoIO technoOutput = new TechnoIO(technoView);
        return ResponseEntity.created(createdTechnoKey.getURI()).body(technoOutput);
    }

    @ApiOperation("Get properties model")
    @GetMapping("/{techno_name}/{techno_version}/{version_type}/model")
    public ResponseEntity<ModelOutput> getModel(@PathVariable("techno_name") final String technoName,
                                                @PathVariable("techno_version") final String technoVersion,
                                                @PathVariable("version_type") final TemplateContainer.VersionType versionType) {

        log.debug("getModel {} {} {}", technoName, technoVersion, versionType);

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, versionType);
        List<AbstractPropertyView> abstractPropertyViews = technoUseCases.getProperties(technoKey);
        ModelOutput modelOutput = new ModelOutput(abstractPropertyViews);
        return ResponseEntity.ok(modelOutput);
    }
}
