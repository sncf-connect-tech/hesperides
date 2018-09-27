/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.tests.bdd.platforms.contexts;

import cucumber.api.java8.En;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.presentation.io.platforms.*;
import org.hesperides.tests.bdd.CucumberSpringBean;
import org.hesperides.tests.bdd.platforms.samples.PlatformSamples;
import org.springframework.http.ResponseEntity;

public class PlatformContext extends CucumberSpringBean implements En {

    private Platform.Key platformKey;

    public PlatformContext() {


        Given("^an existing platform( with differents modules)?", (String differentsModules) -> {
            if(differentsModules != null){
                createPlatform(PlatformSamples.buildPlatformInputWithNameAndDifferentsModules(PlatformSamples.DEFAULT_PLATFORM_NAME));
            } else {
                createPlatform(PlatformSamples.buildPlatformInputWithName(PlatformSamples.DEFAULT_PLATFORM_NAME));
            }
        });
    }

    public Platform.Key getPlatformKey() {
        return platformKey;
    }

    public ResponseEntity<PlatformOutput> createPlatform(PlatformInput platformInput) {
        ResponseEntity<PlatformOutput> response = rest.getTestRest().postForEntity(
                "/applications/{application_name}/platforms", platformInput, PlatformOutput.class, platformInput.getApplicationName());
        PlatformOutput platformOutput = response.getBody();
        platformKey = new Platform.Key(platformOutput.getApplicationName(), platformOutput.getPlatformName());
        return response;
    }

    public ResponseEntity<String> failCreatingPlatform(PlatformInput input) {
        return rest.doWithErrorHandlerDisabled(rest ->
                rest.postForEntity("/applications/{application_name}/platforms", input, String.class, input.getApplicationName())
        );
    }

    public ResponseEntity<PlatformOutput> updatePlatform(PlatformInput platformInput, boolean copyProperties) {
        String url = "/applications/{application_name}/platforms";
        if (copyProperties) {
            url += "?copyPropertiesForUpgradedModules=true";
        }
        return rest.putForEntity(url, platformInput, PlatformOutput.class, platformKey.getApplicationName());
    }

    public ResponseEntity<PlatformOutput> retrieveExistingPlatform() {
        return rest.getTestRest().getForEntity("/applications/{application_name}/platforms/{platform_name}",
                PlatformOutput.class, platformKey.getApplicationName(), platformKey.getPlatformName());
    }

    public ResponseEntity<ApplicationOutput> retrieveExistingApplication() {
        return rest.getTestRest().getForEntity("/applications/{application_name}",
                ApplicationOutput.class, platformKey.getApplicationName());
    }

    public void deleteExistingPlatform() {
        rest.getTestRest().delete("/applications/{application_name}/platforms/{platform_name}",
                platformKey.getApplicationName(), platformKey.getPlatformName());
    }

    public ResponseEntity<ModulePlatformsOutput[]> retrieveExistingPlatformsUsingModule(TemplateContainer.Key moduleKey) {
        return rest.getTestRest()
                .getForEntity("/applications/using_module/{module_name}/{module_version}/{version_type}",
                        ModulePlatformsOutput[].class,
                        moduleKey.getName(), moduleKey.getVersion(), moduleKey.getVersionType().toString());
    }

    public ResponseEntity<SearchResultOutput[]> searchApplication(String search) {
        return rest.getTestRest().postForEntity("/applications/perform_search?name=" + search, null, SearchResultOutput[].class);
    }

    public ResponseEntity<InstanceModelOutput> retriveInstanceModelByPlatformAndModulPath(String path) {
        return rest.getTestRest().getForEntity("/applications/{application_name}/platforms/{platform_name}/properties/instance_model?path={path}",
                InstanceModelOutput.class, platformKey.getApplicationName(), platformKey.getPlatformName(), path);
    }
}
