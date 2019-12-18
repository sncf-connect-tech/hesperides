package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.hesperides.core.application.modules.ModuleUseCases;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.exceptions.TemplateNotFoundException;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.presentation.io.templatecontainers.PartialTemplateIO;
import org.hesperides.core.presentation.io.templatecontainers.TemplateIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Api(tags = "02. Module templates", description = " ")
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
        List<PartialTemplateIO> partialTemplateOutputs = Optional.ofNullable(templateViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PartialTemplateIO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(partialTemplateOutputs);
    }

    @GetMapping("/{module_type}/templates/**")
    @ApiOperation("Get template bundled in a module")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "template_name", required = true, dataType = "string", paramType = "path"),
    })
    public ResponseEntity<TemplateIO> getTemplate(@PathVariable("module_name") final String moduleName,
                                                  @PathVariable("module_version") final String moduleVersion,
                                                  @PathVariable("module_type") final TemplateContainer.VersionType moduleVersionType,
                                                  HttpServletRequest request) {

        String templateName = extractFilePath(request);
        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, moduleVersionType);
        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, templateName)
                .map(TemplateIO::new)
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
        moduleUseCases.createTemplateInWorkingCopy(moduleKey, template, new User(authentication));

        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, template.getName())
                .map(TemplateIO::new)
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
        moduleUseCases.updateTemplateInWorkingCopy(moduleKey, template, new User(authentication));

        TemplateIO templateOutput = moduleUseCases.getTemplate(moduleKey, template.getName())
                .map(TemplateIO::new)
                .orElseThrow(() -> new TemplateNotFoundException(moduleKey, template.getName()));

        return ResponseEntity.ok(templateOutput);
    }

    @DeleteMapping("/workingcopy/templates/**")
    @ApiOperation("Delete template in the working copy of a version")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "template_name", required = true, dataType = "string", paramType = "path"),
    })
    public ResponseEntity<Void> deleteTemplateInWorkingCopy(Authentication authentication,
                                                            @PathVariable("module_name") final String moduleName,
                                                            @PathVariable("module_version") final String moduleVersion,
                                                            HttpServletRequest request) {

        String templateName = extractFilePath(request);
        TemplateContainer.Key moduleKey = new Module.Key(moduleName, moduleVersion, TemplateContainer.VersionType.workingcopy);
        this.moduleUseCases.deleteTemplate(moduleKey, templateName, new User(authentication));

        return ResponseEntity.noContent().build();
    }

    /**
     * Permet d'extraire la PathVariable "templateName", qui peut contenir des slashes.
     */
    private static String extractFilePath(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher();
        path = apm.extractPathWithinPattern(bestMatchPattern, path);
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
