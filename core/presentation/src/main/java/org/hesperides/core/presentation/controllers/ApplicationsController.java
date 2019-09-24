package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.application.security.ApplicationDirectoryGroupsUseCases;
import org.hesperides.core.domain.platforms.queries.views.ApplicationView;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.hesperides.core.presentation.io.platforms.AllApplicationsDetailOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationDirectoryGroupsInput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Slf4j
@Api(tags = "03. Applications", description = " ")
@RequestMapping("/applications")
@RestController
public class ApplicationsController extends AbstractController {

    private final PlatformUseCases platformUseCases;
    private final ApplicationDirectoryGroupsUseCases applicationDirectoryGroupsUseCases;

    @Autowired
    public ApplicationsController(PlatformUseCases platformUseCases, ApplicationDirectoryGroupsUseCases applicationDirectoryGroupsUseCases) {
        this.platformUseCases = platformUseCases;
        this.applicationDirectoryGroupsUseCases = applicationDirectoryGroupsUseCases;
    }

    @GetMapping("")
    @ApiOperation("Get applications")
    public ResponseEntity<List<SearchResultOutput>> getApplications() {
        List<SearchApplicationResultView> applications = platformUseCases.getApplicationNames();

        List<SearchResultOutput> applicationOutputs = applications.stream()
                .distinct()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(applicationOutputs);
    }

    @GetMapping("/{application_name}")
    @ApiOperation("Get application")
    public ResponseEntity<ApplicationOutput> getApplication(@PathVariable("application_name") final String applicationName,
                                                            @RequestParam(value = "hide_platform", required = false) final Boolean hidePlatformsModules,
                                                            @RequestParam(value = "with_password_info", required = false) final Boolean withPasswordFlag) {

        ApplicationView application = platformUseCases.getApplication(
                applicationName,
                Boolean.TRUE.equals(hidePlatformsModules),
                Boolean.TRUE.equals(withPasswordFlag));

        return ResponseEntity.ok(new ApplicationOutput(application));
    }

    @ApiOperation("Deprecated - Use GET /applications/perform_search instead")
    @PostMapping("/perform_search")
    @Deprecated
    public ResponseEntity<List<SearchResultOutput>> postSearchApplications(@RequestParam("name") String applicationName) {
        return ResponseEntity.ok()
                .header("Deprecation", "version=\"2019-07-30\"")
                .header("Sunset", "Wed Jul 31 00:00:00 CEST 2020")
                .header("Link", "/perform_search")
                .body(searchApplications(applicationName).getBody());
    }

    @ApiOperation("Search applications")
    @GetMapping("/perform_search")
    public ResponseEntity<List<SearchResultOutput>> searchApplications(@RequestParam("name") String applicationName) {

        List<SearchApplicationResultView> searchApplicationResultViews = platformUseCases.searchApplications(defaultString(applicationName, ""));

        List<SearchResultOutput> searchResultOutputs = Optional.ofNullable(searchApplicationResultViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .distinct()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(searchResultOutputs);
    }


    @ApiOperation("Update the directory groups of an application")
    @PutMapping("/{application_name}/directory_groups")
    public ResponseEntity<Map<String, List<String>>> setDirectoryGroups(Authentication authentication,
                                                                        @PathVariable("application_name") final String applicationName,
                                                                        @Valid @RequestBody final ApplicationDirectoryGroupsInput applicationDirectoryGroupsInput) {

        applicationDirectoryGroupsUseCases.setApplicationDirectoryGroups(
                applicationName,
                applicationDirectoryGroupsInput.getDirectoryGroups(),
                new User(authentication));

        ApplicationDirectoryGroupsView applicationDirectoryGroups = applicationDirectoryGroupsUseCases.getApplicationDirectoryGroups(applicationName).get();
        return ResponseEntity.ok(applicationDirectoryGroups.getDirectoryGroupCNs());
    }

    @ApiOperation("Get all applications, their platforms and their modules (with a cache)")
    @GetMapping("/platforms")
    @Cacheable("all-applications-detail")
    public ResponseEntity<AllApplicationsDetailOutput> getAllApplicationsDetail(@RequestParam(value = "with_password_info", required = false) final Boolean withPasswordFlag) {

        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(utcTimeZone);
        String nowAsIso = df.format(new Date());

        final List<ApplicationOutput> applications = platformUseCases.getAllApplicationsDetail(Boolean.TRUE.equals(withPasswordFlag))
                .stream()
                .map(ApplicationOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new AllApplicationsDetailOutput(nowAsIso, applications));
    }
}
