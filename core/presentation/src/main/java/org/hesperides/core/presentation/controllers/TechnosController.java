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
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.technos.TechnoUseCases;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.exception.TechnoNotFoundException;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
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


@Slf4j
@Api(tags = "07. Technos", description = " ")
@RequestMapping({"/templates/packages", "/technos"})
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
        technoUseCases.addTemplate(technoKey, templateInput.toDomainInstance(technoKey), new User(authentication));
        TemplateIO templateOutput = technoUseCases.getTemplate(technoKey, templateInput.getName())
                .map(TemplateIO::new)
                .orElseThrow(() -> new TemplateNotFoundException(technoKey, templateInput.getName()));

        return ResponseEntity.created(technoKey.getURI()).body(templateOutput);
    }

    @ApiOperation("Get all techno names")
    @GetMapping
    public ResponseEntity<List<String>> getTechnosName() {
        log.debug("getTechnosName");

        List<String> technosNames = technoUseCases.getTechnosName();
        log.debug("return getTechnosName: {}", technosNames.toString());

        return ResponseEntity.ok(technosNames);
    }

    @ApiOperation("Get all versions for a given techno")
    @GetMapping("/{techno_name}")
    public ResponseEntity<List<String>> getTechnoVersions(@PathVariable("techno_name") final String technoName) {

        log.debug("getTechnoVersions technoName: {}", technoName);

        List<String> technoVersions = technoUseCases.getTechnoVersions(technoName);
        log.debug("return getTechnoVersions: {}", technoVersions.toString());

        return ResponseEntity.ok(technoVersions);
    }

    @ApiOperation("Get all types for a given techno version")
    @GetMapping("/{techno_name}/{techno_version:.+}")
    public ResponseEntity<List<String>> getTechnoTypes(@PathVariable("techno_name") final String technoName,
                                                       @PathVariable("techno_version") final String technoVersion) {

        log.debug("getTechnoTypes technoName: {}, technoVersion: {}", technoName, technoVersion);

        List<String> technoTypes = technoUseCases.getTechnoTypes(technoName, technoVersion);
        log.debug("return getTechnoTypes: {}", technoTypes.toString());

        return ResponseEntity.ok(technoTypes);
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

    @ApiOperation("Delete a techno")
    @DeleteMapping("/{techno_name}/{techno_version}/{version_type}")
    public ResponseEntity<Void> deleteTechno(Authentication authentication,
                                             @PathVariable("techno_name") final String technoName,
                                             @PathVariable("techno_version") final String technoVersion,
                                             @PathVariable("version_type") final TemplateContainer.VersionType versionType) {

        log.info("deleteTechno {} {} {}", technoName, technoVersion, versionType);

        TemplateContainer.Key technoKey = new Techno.Key(technoName, technoVersion, versionType);
        technoUseCases.deleteTechno(technoKey, new User(authentication));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Create a release from an existing workingcopy")
    @PostMapping("/create_release")
    public ResponseEntity<TechnoIO> releaseTechno(Authentication authentication,
                                                  @RequestParam("techno_name") final String technoName,
                                                  @RequestParam("techno_version") final String technoVersion) {

        log.info("releaseTechno {} {}", technoName, technoVersion);

        TemplateContainer.Key existingTechnoKey = new Techno.Key(technoName, technoVersion, TemplateContainer.VersionType.workingcopy);
        TechnoView technoView = technoUseCases.releaseTechno(existingTechnoKey, new User(authentication));
        TechnoIO technoOutput = new TechnoIO(technoView);

        URI releasedTechnoLocation = technoView.toDomainInstance().getKey().getURI();
        return ResponseEntity.created(releasedTechnoLocation).body(technoOutput);
    }

    @ApiOperation("Deprecated - Use GET /technos/perform_search instead")
    @PostMapping("/perform_search")
    @Deprecated
    public ResponseEntity<List<TechnoIO>> postSearch(@ApiParam(value = "Format: name (+ version)", required = true)
                                                     @RequestParam final String terms) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-04-24\"")
                .header("Sunset", "Wed Apr 25 00:00:00 CEST 2020")
                .header("Link", "/technos/perform_search")
                .body(search(terms, 0).getBody());
    }

    @ApiOperation("Search for technos")
    @GetMapping("/perform_search")
    public ResponseEntity<List<TechnoIO>> search(@ApiParam(value = "Format: name (+ version)", required = true)
                                                 @RequestParam final String terms,
                                                 @RequestParam(required = false) final Integer size) {

        log.debug("search technos {}", terms);

        List<TechnoView> technoViews = technoUseCases.search(terms, size);
        List<TechnoIO> technoOutputs = Optional.ofNullable(technoViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(TechnoIO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(technoOutputs);
    }

    @ApiOperation("Create a copy of a techno")
    @PostMapping
    public ResponseEntity<TechnoIO> copyTechno(Authentication authentication,
                                               @RequestParam("from_name") final String fromTechnoName,
                                               @RequestParam("from_version") final String fromTechnoVersion,
                                               @RequestParam("from_is_working_copy") final Boolean isFromWorkingCopy,
                                               @Valid @RequestBody final TechnoIO technoInput) {

        log.info("copyTechno {}", technoInput.toString());

        TemplateContainer.Key existingTechnoKey = new Techno.Key(fromTechnoName, fromTechnoVersion, TemplateContainer.getVersionType(isFromWorkingCopy));
        TemplateContainer.Key newTechnoKey = new Techno.Key(technoInput.getName(), technoInput.getVersion(), TemplateContainer.VersionType.workingcopy);
        TechnoView technoView = technoUseCases.createWorkingCopyFrom(existingTechnoKey, newTechnoKey, new User(authentication));
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
