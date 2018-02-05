package org.hesperides.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hesperides.application.Modules;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.ModuleType;
import org.hesperides.domain.modules.Template;
import org.hesperides.domain.modules.exceptions.TemplateWasNotFoundException;
import org.hesperides.domain.modules.queries.TemplateView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.net.URI;
import java.util.function.BiConsumer;

import static org.hesperides.domain.modules.ModuleType.workingcopy;
import static org.springframework.http.HttpStatus.SEE_OTHER;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@Api("/modules")
@RestController
@RequestMapping("/modules/{module_name}/{module_version}")
public class TemplateController extends BaseResource {

    private final Modules modules;

    public TemplateController(Modules modules) {
        this.modules = modules;
    }

    @PostMapping("/workingcopy/templates")
    @ApiOperation("Create template in the workingcopy of a module")
    public ResponseEntity createTemplateInWorkingCopy(
            @PathVariable("module_name") final String moduleName,
            @PathVariable("module_version") final String moduleVersion,
            @Valid @RequestBody final TemplateInput templateInput) throws Throwable {

        // map input to domain instance:
        Template template = templateInput.toDomainInstance();

        modules.createTemplateInWorkingCopy(new Module.Key(moduleName, moduleVersion, ModuleType.workingcopy), template);
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

        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, workingcopy);
        return modules.getTemplate(moduleKey, templateName).orElseThrow(() -> new TemplateWasNotFoundException(moduleKey, templateName));
    }

    @DeleteMapping("/workingcopy/templates/{template_name}")
    @ApiOperation("Delete template in the working copy of a version")
    public ResponseEntity deleteTemplateInWorkingCopy(
                                            @PathVariable("module_name") final String moduleName,
                                            @PathVariable("module_version") final String moduleVersion,
                                            @PathVariable("template_name") final String templateName) throws Throwable {

        this.modules.deleteTemplate(new Module.Key(moduleName, moduleVersion, ModuleType.workingcopy), templateName);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/workingcopy/templates")
    @ApiOperation("Update template in the workingcopy of a module")
    public DeferredResult<ResponseEntity> updateTemplateInWorkingCopy(
                                                @PathVariable("module_name") final String moduleName,
                                                @PathVariable("module_version") final String moduleVersion,
                                                @Valid @RequestBody final TemplateInput templateInput) throws Throwable {
        // map input to domain instance:
        Template template = templateInput.toDomainInstance();

        DeferredResult<ResponseEntity> result = new DeferredResult<>();
        modules.updateTemplateInWorkingCopy(new Module.Key(moduleName, moduleVersion, ModuleType.workingcopy), template)
                .thenApply(o -> {
                    URI location = fromPath("/rest/modules/{module_name}/{module_version}/workingcopy/templates/{template_name}")
                            .buildAndExpand(moduleName, moduleVersion, template.getName()).toUri();
                    return ResponseEntity.status(SEE_OTHER).location(location).build();
                })
                .whenCompleteAsync(completeDeferredResult(result));
        return result;
    }

    private BiConsumer<Object, Throwable> completeDeferredResult(DeferredResult result) {
        return (o, throwable) -> {
            result.setErrorResult(throwable);
            result.setErrorResult(o);
        };
    }
}
