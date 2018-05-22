package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hesperides.application.modules.ModuleUseCases;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hesperides.presentation.io.PartialTemplateIO;
import org.hesperides.presentation.io.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.domain.security.User.fromPrincipal;

@Api("/modules")
@RestController
@RequestMapping("/modules/{module_name}/{module_version}")
@CrossOrigin
public class TemplateController extends BaseController {

    private final ModuleUseCases moduleUseCases;

    @Autowired
    public TemplateController(ModuleUseCases moduleUseCases) {
        this.moduleUseCases = moduleUseCases;
    }

    @GetMapping("/{module_type}/templates")
    @ApiOperation("Get all templates bundled in a module")
    public ResponseEntity<List<PartialTemplateIO>> getModuleTemplates(@PathVariable("module_name") final String moduleName,
                                                                      @PathVariable("module_version") final String moduleVersion,
                                                                      @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType) {

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        List<TemplateView> templateViews = moduleUseCases.getTemplates(moduleKey);
        List<PartialTemplateIO> partialTemplateOutput = templateViews != null
                ? templateViews.stream().map(PartialTemplateIO::fromTemplateView).collect(Collectors.toList())
                : new ArrayList<>();

        return ResponseEntity.ok(partialTemplateOutput);
    }

    @GetMapping("/{module_type}/templates/{template_name}")
    @ApiOperation("Get template bundled in a module for a version workingcopy")
    public ResponseEntity<TemplateIO> getTemplateInWorkingCopy(@PathVariable("module_name") final String moduleName,
                                                               @PathVariable("module_version") final String moduleVersion,
                                                               @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType,
                                                               @PathVariable("template_name") final String templateName) {

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, templateName)
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(moduleKey, templateName));

        return ResponseEntity.ok(templateOutput);
    }

    @PostMapping("/workingcopy/templates")
    @ApiOperation("Create template in the workingcopy of a module")
    public ResponseEntity<TemplateIO> createTemplateInWorkingCopy(Principal currentUser,
                                                                  @PathVariable("module_name") final String moduleName,
                                                                  @PathVariable("module_version") final String moduleVersion,
                                                                  @Valid @RequestBody final TemplateIO templateInput) {

        final Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        Template template = templateInput.toDomainInstance(moduleKey);
        moduleUseCases.createTemplateInWorkingCopy(moduleKey, template, fromPrincipal(currentUser));

        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, template.getName())
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(moduleKey, template.getName()));

        return ResponseEntity.created(template.getTemplateContainerKey().getURI(Module.KEY_PREFIX)).body(templateOutput);
    }

    @PutMapping("/workingcopy/templates")
    @ApiOperation("Update template in the workingcopy of a module")
    public ResponseEntity<TemplateIO> updateTemplateInWorkingCopy(Principal currentUser,
                                                                  @PathVariable("module_name") final String moduleName,
                                                                  @PathVariable("module_version") final String moduleVersion,
                                                                  @Valid @RequestBody final TemplateIO templateInput) {

        final Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        Template template = templateInput.toDomainInstance(moduleKey);
        moduleUseCases.updateTemplateInWorkingCopy(moduleKey, template, fromPrincipal(currentUser));

        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, template.getName())
                .map(TemplateIO::fromTemplateView)
                .orElseThrow(() -> new TemplateNotFoundException(moduleKey, template.getName()));

        return ResponseEntity.ok(templateOutput);
    }

    @DeleteMapping("/workingcopy/templates/{template_name}")
    @ApiOperation("Delete template in the working copy of a version")
    public ResponseEntity deleteTemplateInWorkingCopy(Principal currentUser,
                                                      @PathVariable("module_name") final String moduleName,
                                                      @PathVariable("module_version") final String moduleVersion,
                                                      @PathVariable("template_name") final String templateName) {

        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        this.moduleUseCases.deleteTemplate(moduleKey, templateName, fromPrincipal(currentUser));

        return ResponseEntity.noContent().build();
    }
}
