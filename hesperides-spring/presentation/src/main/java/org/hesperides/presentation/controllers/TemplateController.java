package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hesperides.application.ModuleUseCases;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.domain.modules.queries.TemplateView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.springframework.http.HttpStatus.SEE_OTHER;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@Api("/modules")
@RestController
@RequestMapping("/modules/{module_name}/{module_version}")
public class TemplateController extends BaseResource {

    private final ModuleUseCases moduleUseCases;

    public TemplateController(ModuleUseCases moduleUseCases) {
        this.moduleUseCases = moduleUseCases;
    }

    @PostMapping("/workingcopy/templates")
    @ApiOperation("Create template in the workingcopy of a module")
    public ResponseEntity createTemplateInWorkingCopy(
            @PathVariable("module_name") final String moduleName,
            @PathVariable("module_version") final String moduleVersion,
            @Valid @RequestBody final TemplateInput templateInput) {

        // map input to domain instance:
        Template template = templateInput.toDomainInstance();

        moduleUseCases.createTemplateInWorkingCopy(new Module.Key(moduleName, moduleVersion, Module.Type.workingcopy), template);
        URI location = fromPath("/rest/modules/{module_name}/{module_version}/workingcopy/templates/{template_name}")
                .buildAndExpand(moduleName, moduleVersion, template.getName()).toUri();
        return ResponseEntity.status(SEE_OTHER).location(location).build();
    }

    @GetMapping("/workingcopy/templates/{template_name}")
    @ApiOperation("Get template bundled in a module for a version workingcopy")
    public TemplateView getTemplateInWorkingCopy(
            @PathVariable("module_name") final String moduleName,
            @PathVariable("module_version") final String moduleVersion,
            @PathVariable("template_name") final String templateName) {

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, Module.Type.workingcopy);
        return moduleUseCases.getTemplate(moduleKey, templateName).orElseThrow(() -> new TemplateNotFoundException(moduleKey, templateName));
    }

    @DeleteMapping("/workingcopy/templates/{template_name}")
    @ApiOperation("Delete template in the working copy of a version")
    public ResponseEntity deleteTemplateInWorkingCopy(
            @PathVariable("module_name") final String moduleName,
            @PathVariable("module_version") final String moduleVersion,
            @PathVariable("template_name") final String templateName) {

        this.moduleUseCases.deleteTemplate(new Module.Key(moduleName, moduleVersion, Module.Type.workingcopy), templateName);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/workingcopy/templates")
    @ApiOperation("Update template in the workingcopy of a module")
    public ResponseEntity updateTemplateInWorkingCopy(
            @PathVariable("module_name") final String moduleName,
            @PathVariable("module_version") final String moduleVersion,
            @Valid @RequestBody final TemplateInput templateInput) {
        // map input to domain instance:
        Template template = templateInput.toDomainInstance();
        moduleUseCases.updateTemplateInWorkingCopy(new Module.Key(moduleName, moduleVersion, Module.Type.workingcopy), template);
        URI location = fromPath("/rest/modules/{module_name}/{module_version}/workingcopy/templates/{template_name}")
                .buildAndExpand(moduleName, moduleVersion, template.getName()).toUri();
        return ResponseEntity.status(SEE_OTHER).location(location).build();
    }
}
