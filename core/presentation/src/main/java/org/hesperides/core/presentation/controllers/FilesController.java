package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.files.FileUseCases;
import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Api("/files")
@RequestMapping("/files")
@RestController
public class FilesController extends AbstractController {

    private FileUseCases filesUseCases;

    @Autowired
    public FilesController(final FileUseCases filesUseCases) {
        this.filesUseCases = filesUseCases;
    }

    @ApiOperation("Get files list for given application, version, platefome, unit and context")
    @GetMapping("/applications/{application_name}/platforms/{platform_name}/{path}/{module_name}/{module_version}/instances/{instance_name}")
    public ResponseEntity<List<InstanceFileOutput>> getInstanceFiles(@PathVariable("application_name") final String applicationName,
                                                                     @PathVariable("platform_name") final String platformName,
                                                                     @PathVariable("path") final String modulePath,
                                                                     @PathVariable("module_name") final String moduleName,
                                                                     @PathVariable("module_version") final String moduleVersion,
                                                                     @PathVariable("instance_name") final String instanceName,
                                                                     @RequestParam("isWorkingCopy") final Boolean isWorkingCopy,
                                                                     @RequestParam(value = "simulate", required = false) final Boolean simulate) {

        List<InstanceFileOutput> files = filesUseCases.getInstanceFiles(
                applicationName,
                platformName,
                modulePath,
                moduleName,
                moduleVersion,
                instanceName,
                isWorkingCopy,
                Boolean.TRUE.equals(simulate))
                .stream()
                .map(InstanceFileOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(files);
    }
}
