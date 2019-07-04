package org.hesperides.core.presentation.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.application.authorizations.AuthorizationUseCases;
import org.hesperides.core.application.platforms.PlatformUseCases;
import org.hesperides.core.domain.platforms.queries.views.ApplicationView;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.presentation.io.platforms.ApplicationAuthoritiesInput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        List<SearchApplicationResultView> apps = platformUseCases.listApplications();

        List<SearchResultOutput> appsOutput = apps.stream()
                .distinct()
                .map(SearchResultOutput::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appsOutput);
    }

    @GetMapping("/{application_name}")
    @ApiOperation("Get application")
    public ResponseEntity<ApplicationOutput> getApplication(@PathVariable("application_name") final String applicationName,
                                                            @RequestParam(value = "hide_platform", required = false) final Boolean hidePlatformsModules,
                                                            @RequestParam(value = "with_password_count", required = false) final Boolean withPasswordCount) {

        ApplicationView applicationView = platformUseCases.getApplication(applicationName);
        Map<String, List<String>> applicationAuthorities = authorizationUseCases.getApplicationAuthorities(applicationName);
        Integer passwordCount = Boolean.TRUE.equals(withPasswordCount) ? platformUseCases.countModulesAndTehnosPasswords(applicationView) : null;
        ApplicationOutput applicationOutput = new ApplicationOutput(applicationView, Boolean.TRUE.equals(hidePlatformsModules), applicationAuthorities, passwordCount);

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

    @ApiOperation("Update authorities of an application")
    @PutMapping("/{application_name}/authorities")
    public ResponseEntity updateAuthorities(Authentication authentication,
                                            @PathVariable("application_name") final String applicationName,
                                            @Valid @RequestBody final ApplicationAuthoritiesInput applicationAuthoritiesInput) {
        authorizationUseCases.updateApplicationAuthorities(applicationName, applicationAuthoritiesInput.getAuthorities());
        return ResponseEntity.ok().build();
    }
}
