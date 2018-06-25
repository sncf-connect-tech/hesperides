package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hesperides.application.modules.ModuleUseCases;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.hesperides.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.presentation.io.templatecontainers.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.domain.security.User.fromAuthentication;

@Api("/modules")
@RequestMapping("/modules/{module_name}/{module_version}")
@RestController
public class ModuleTemplatesController extends AbstractController {

    private final ModuleUseCases moduleUseCases;

    @Autowired
    public ModuleTemplatesController(ModuleUseCases moduleUseCases) {
        this.moduleUseCases = moduleUseCases;
    }

    @GetMapping("/{module_type}/templates")
    @ApiOperation("Get all templates bundled in a module")
    public ResponseEntity<List<PartialTemplateIO>> getModuleTemplates(@PathVariable("module_name") final String moduleName,
                                                                      @PathVariable("module_version") final String moduleVersion,
                                                                      @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType) {

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        List<TemplateView> templateViews = moduleUseCases.getTemplates(moduleKey);
        List<PartialTemplateIO> partialTemplateOutputs = templateViews != null
                ? templateViews.stream().map(PartialTemplateIO::fromTemplateView).collect(Collectors.toList())
                : new ArrayList<>();

        return ResponseEntity.ok(partialTemplateOutputs);
    }

    @GetMapping("/{module_type}/templates/{template_name:.+}")
    @ApiOperation("Get template bundled in a module for a version workingcopy")
    public ResponseEntity<TemplateIO> getTemplateInWorkingCopy(@PathVariable("module_name") final String moduleName,
                                                               @PathVariable("module_version") final String moduleVersion,
                                                               @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType,
                                                               @PathVariable("template_name") final String templateName) {

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, templateName)
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(moduleKey, templateName));

        return ResponseEntity.ok(templateOutput);
    }

    @PostMapping("/workingcopy/templates")
    @ApiOperation("Create template in the workingcopy of a module")
    public ResponseEntity<TemplateIO> createTemplateInWorkingCopy(Authentication authentication,
                                                                  @PathVariable("module_name") final String moduleName,
                                                                  @PathVariable("module_version") final String moduleVersion,
                                                                  @Valid @RequestBody final TemplateIO templateInput) {

        final TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        Template template = templateInput.toDomainInstance(moduleKey);
        moduleUseCases.createTemplateInWorkingCopy(moduleKey, template, fromAuthentication(authentication));

        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, template.getName())
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(moduleKey, template.getName()));

        return ResponseEntity.created(template.getTemplateContainerKey().getURI()).body(templateOutput);
    }

    @PutMapping("/workingcopy/templates")
    @ApiOperation("Update template in the workingcopy of a module")
    public ResponseEntity<TemplateIO> updateTemplateInWorkingCopy(Authentication authentication,
                                                                  @PathVariable("module_name") final String moduleName,
                                                                  @PathVariable("module_version") final String moduleVersion,
                                                                  @Valid @RequestBody final TemplateIO templateInput) {

        final TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        Template template = templateInput.toDomainInstance(moduleKey);
        moduleUseCases.updateTemplateInWorkingCopy(moduleKey, template, fromAuthentication(authentication));

        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, template.getName())
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(moduleKey, template.getName()));

        return ResponseEntity.ok(templateOutput);
    }

    @DeleteMapping("/workingcopy/templates/{template_name:.+}")
    @ApiOperation("Delete template in the working copy of a version")
    public ResponseEntity deleteTemplateInWorkingCopy(Authentication authentication,
                                                      @PathVariable("module_name") final String moduleName,
                                                      @PathVariable("module_version") final String moduleVersion,
                                                      @PathVariable("template_name") final String templateName) {

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        this.moduleUseCases.deleteTemplate(moduleKey, templateName, fromAuthentication(authentication));

        return ResponseEntity.noContent().build();
    }
}
