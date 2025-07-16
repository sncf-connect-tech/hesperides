package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.files.FileUseCases;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Api(tags = "06. Files", description = " ")
@RequestMapping("/")
@RestController
public class FilesController extends AbstractController {

    private final FileUseCases filesUseCases;

    @Autowired
    public FilesController(final FileUseCases filesUseCases) {
        this.filesUseCases = filesUseCases;
    }

    @Deprecated
    @ApiOperation("Deprecated - Use GET /files/applications/{application_name}/platforms/{platform_name}/{module_path}/{module_name}/{module_version}/instances/{instance_name}/files instead")
    @GetMapping("files/applications/{application_name}/platforms/{platform_name}/{module_path}/{module_name}/{module_version}/instances/{instance_name}")
    public ResponseEntity<List<InstanceFileOutput>> getInstanceFilesDeprecated(@PathVariable("application_name") final String applicationName,
                                                                               @PathVariable("platform_name") final String platformName,
                                                                               @PathVariable("module_path") final String modulePath,
                                                                               @PathVariable("module_name") final String moduleName,
                                                                               @PathVariable("module_version") final String moduleVersion,
                                                                               @PathVariable("instance_name") final String instanceName,
                                                                               @RequestParam("isWorkingCopy") final Boolean isWorkingCopy,
                                                                               @ApiParam(value = "Use module values if instance does not exist")
                                                                               @RequestParam(value = "simulate", required = false, defaultValue = "false") final String simulate) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-09-24\"")
                .header("Sunset", "Wed Sep 25 00:00:00 CEST 2020")
                .header("Link", String.format("/applications/%s/platforms/%s/%s/%s/%s/instances/%s/files", applicationName, platformName, modulePath, moduleName, moduleVersion, instanceName))
                .body(getInstanceFiles(applicationName, platformName, modulePath, moduleName, moduleVersion, instanceName, isWorkingCopy, simulate).getBody());

    }

    @Deprecated
    @ApiOperation("Deprecated - Use GET /files/applications/{application_name}/platforms/{platform_name}/{module_path}/{module_name}/{module_version}/instances/{instance_name}/files/{template_name} instead")
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8", path =
            "files/applications/{application_name}/platforms/{platform_name}/{module_path}/{module_name}/{module_version}/instances/{instance_name}/{template_name}")
    public ResponseEntity<String> getFileDeprecated(Authentication authentication,
                                                    @PathVariable("application_name") final String applicationName,
                                                    @PathVariable("platform_name") final String platformName,
                                                    @PathVariable("module_path") final String modulePath,
                                                    @PathVariable("module_name") final String moduleName,
                                                    @PathVariable("module_version") final String moduleVersion,
                                                    @PathVariable("instance_name") final String instanceName,
                                                    @PathVariable("template_name") final String templateName,
                                                    @RequestParam("isWorkingCopy") final Boolean isWorkingCopy,
                                                    @RequestParam("template_namespace") final String templateNamespace,
                                                    @ApiParam(value = "Use module values if instance does not exist")
                                                    @RequestParam(value = "simulate", required = false) final Boolean simulate) {

        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-09-24\"")
                .header("Sunset", "Wed Sep 25 00:00:00 CEST 2020")
                .header("Link", String.format("/applications/%s/platforms/%s/%s/%s/%s/instances/%s/files/%s", applicationName, platformName, modulePath, moduleName, moduleVersion, instanceName, templateName))
                .body(getFile(authentication, applicationName, platformName, modulePath, moduleName, moduleVersion, instanceName, templateName, isWorkingCopy, templateNamespace, simulate).getBody());
    }

    @ApiOperation("Get the list of files of an instance or a module")
    @GetMapping("applications/{application_name}/platforms/{platform_name}/{module_path}/{module_name}/{module_version}/instances/{instance_name}/files")
    public ResponseEntity<List<InstanceFileOutput>> getInstanceFiles(@PathVariable("application_name") final String applicationName,
                                                                     @PathVariable("platform_name") final String platformName,
                                                                     @PathVariable("module_path") final String modulePath,
                                                                     @PathVariable("module_name") final String moduleName,
                                                                     @PathVariable("module_version") final String moduleVersion,
                                                                     @PathVariable("instance_name") final String instanceName,
                                                                     @RequestParam("isWorkingCopy") final Boolean isWorkingCopy,
                                                                     @ApiParam(value = "Use module values if instance does not exist")
                                                                     @RequestParam(value = "simulate", required = false, defaultValue = "false") final String simulate) {

        // Pour des raisons de retrocompatibilité avec le front,
        // en attendant que https://github.com/sncf-connect-tech/hesperides-gui/pull/164 soit en prod,
        // nous devons pour le moment supporter simulate=undefined en valeur de paramètre
        List<InstanceFileOutput> files = filesUseCases.getFiles(
                applicationName,
                platformName,
                modulePath,
                moduleName,
                moduleVersion,
                instanceName,
                Boolean.TRUE.equals(isWorkingCopy),
                "true".equals(simulate))
                .stream()
                .map(InstanceFileOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(files);
    }

    @ApiOperation("Get a valued template file")
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8", path =
            "applications/{application_name}/platforms/{platform_name}/{module_path}/{module_name}/{module_version}/instances/{instance_name}/files/{template_name}")
    public ResponseEntity<String> getFile(Authentication authentication,
                                          @PathVariable("application_name") final String applicationName,
                                          @PathVariable("platform_name") final String platformName,
                                          @PathVariable("module_path") final String modulePath,
                                          @PathVariable("module_name") final String moduleName,
                                          @PathVariable("module_version") final String moduleVersion,
                                          @PathVariable("instance_name") final String instanceName,
                                          @PathVariable("template_name") final String templateName,
                                          @RequestParam("isWorkingCopy") final Boolean isWorkingCopy,
                                          @RequestParam("template_namespace") final String templateNamespace,
                                          @ApiParam(value = "Use module values if instance does not exist")
                                          @RequestParam(value = "simulate", required = false) final Boolean simulate) {

        String fileContent = filesUseCases.getFile(
                applicationName,
                platformName,
                modulePath,
                moduleName,
                moduleVersion,
                instanceName,
                templateName,
                Boolean.TRUE.equals(isWorkingCopy),
                templateNamespace,
                Boolean.TRUE.equals(simulate),
                new User(authentication));

        return ResponseEntity.ok(fileContent);
    }

}
