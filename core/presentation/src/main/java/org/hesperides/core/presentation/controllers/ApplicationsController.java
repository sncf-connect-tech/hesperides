package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.authorizations.AuthorizationUseCases;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.platforms.queries.views.ApplicationView;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.queries.views.ApplicationAuthoritiesView;
import org.hesperides.core.presentation.io.platforms.AllApplicationsDetailOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationAuthoritiesInput;
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
@Api(tags = "3. Applications", description = " ")
@RequestMapping("/applications")
@RestController
public class ApplicationsController extends AbstractController {

    private final PlatformUseCases platformUseCases;
    private final AuthorizationUseCases authorizationUseCases;

    @Autowired
    public ApplicationsController(PlatformUseCases platformUseCases, AuthorizationUseCases authorizationUseCases) {
        this.platformUseCases = platformUseCases;
        this.authorizationUseCases = authorizationUseCases;
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
                                                            @RequestParam(value = "with_password_count", required = false) final Boolean withPasswordCount) {

        ApplicationView application = platformUseCases.getApplication(applicationName);
        Map<String, List<String>> applicationAuthorities = authorizationUseCases.getApplicationAuthorities(applicationName)
                .map(ApplicationAuthoritiesView::getAuthorities)
                .orElse(Collections.emptyMap());
        Integer passwordCount = Boolean.TRUE.equals(withPasswordCount) ? platformUseCases.countModulesAndTechnosPasswords(application) : null;

        ApplicationOutput applicationOutput = new ApplicationOutput(application,
                Boolean.TRUE.equals(hidePlatformsModules),
                applicationAuthorities,
                passwordCount);

        return ResponseEntity.ok(applicationOutput);
    }

    @ApiOperation("Deprecated - Use GET /applications/perform_search instead")
    @PostMapping("/perform_search")
    @Deprecated
    public ResponseEntity<List<SearchResultOutput>> postSearchApplications(@RequestParam("name") String applicationName) {
        return searchApplications(applicationName);
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


    @ApiOperation("Update the authorities of an application")
    @PutMapping("/{application_name}/authorities")
    public ResponseEntity setAuthorities(Authentication authentication,
                                         @PathVariable("application_name") final String applicationName,
                                         @Valid @RequestBody final ApplicationAuthoritiesInput applicationAuthoritiesInput) {

        authorizationUseCases.setApplicationAuthorities(
                applicationName,
                applicationAuthoritiesInput.getAuthorities(),
                new User(authentication));

        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get all applications, their platforms and their modules (with a cache)")
    @GetMapping("/platforms")
    @Cacheable("all-applications-detail")
    public ResponseEntity<AllApplicationsDetailOutput> getAllApplicationsDetail() {

        TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(utcTimeZone);
        String nowAsIso = df.format(new Date());

        final List<ApplicationOutput> applications = platformUseCases.getAllApplicationsDetail()
                .stream()
                .map(application -> new ApplicationOutput(application, false, Collections.emptyMap(), null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new AllApplicationsDetailOutput(nowAsIso, applications));
    }
}
