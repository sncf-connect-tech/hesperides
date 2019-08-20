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
package org.hesperides.test.bdd.platforms;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.platforms.*;
import org.hesperides.core.presentation.io.platforms.properties.GlobalPropertyUsageOutput;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.hesperides.core.presentation.io.platforms.properties.diff.PropertiesDiffOutput;
import org.hesperides.test.bdd.commons.CustomRestTemplate;
import org.hesperides.test.bdd.commons.TestContext;
import org.hesperides.test.bdd.templatecontainers.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static org.hesperides.test.bdd.commons.HesperidesScenario.getResponseType;

@Component
public class PlatformClient {

    private final CustomRestTemplate restTemplate;
    private final TestContext testContext;

    @Autowired
    public PlatformClient(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") CustomRestTemplate restTemplate, TestContext testContext) {
        this.restTemplate = restTemplate;
        this.testContext = testContext;
    }

    public void createPlatform(PlatformIO platformInput) {
        createPlatform(platformInput, null);
    }

    public void createPlatform(PlatformIO platformInput, String tryTo) {
        restTemplate.postForEntity(
                "/applications",
                platformInput,
                getResponseType(tryTo, PlatformIO.class));
    }

    public void copyPlatform(PlatformIO existingPlatform, PlatformIO newPlatform, boolean withoutInstancesOrProperties, String tryTo) {
        String url = "/applications/{application_name}/platforms?from_application={from_application}&from_platform={from_platform}";
        if (withoutInstancesOrProperties) {
            url += "&copy_instances_and_properties=false";
        }
        restTemplate.postForEntity(
                url,
                newPlatform,
                getResponseType(tryTo, PlatformIO.class),
                existingPlatform.getApplicationName(),
                existingPlatform.getApplicationName(),
                existingPlatform.getPlatformName());
    }

    public void deletePlatform(PlatformIO platformInput, String tryTo) {
        restTemplate.deleteForEntity(
                "/applications/{application_name}/platforms/{platform_name}",
                getResponseType(tryTo, ResponseEntity.class),
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public void getPlatform(PlatformIO platformInput, Long timestamp, boolean withPasswordFlag, String tryTo) {
        String url = "/applications/{application_name}/platforms/{platform_name}";
        if (timestamp != null) {
            url += "?timestamp=" + timestamp;
        }
        if (withPasswordFlag) {
            url += "?with_password_info=true";
        }
        restTemplate.getForEntity(
                url,
                getResponseType(tryTo, PlatformIO.class),
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public void updatePlatform(PlatformIO platformInput, String tryTo) {
        updatePlatform(platformInput, false, tryTo);
    }

    public void updatePlatform(PlatformIO platformInput, boolean copyProperties, String tryTo) {
        String url = "/applications/{application_name}/platforms";
        if (copyProperties) {
            url += "?copyPropertiesForUpgradedModules=true";
        }
        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(platformInput),
                getResponseType(tryTo, PlatformIO.class),
                platformInput.getApplicationName());
    }

    public void searchApplication(String search, String tryTo) {
        restTemplate.postForEntity("/applications/perform_search?name=" + search, null, getResponseType(tryTo, SearchResultOutput[].class));
    }

    public void searchPlatform(String applicationName, String platformName, String tryTo) {
        String url = "/applications/platforms/perform_search?applicationName=" + applicationName;
        if (StringUtils.isNotBlank(platformName)) {
            url += "&platformName=" + platformName;
        }
        restTemplate.postForEntity(
                url,
                null,
                getResponseType(tryTo, SearchResultOutput[].class));
    }

    public void getInstancesModel(PlatformIO platform, String propertiesPath) {
        restTemplate.getForEntity(
                "/applications/{application_name}/platforms/{platform_name}/properties/instance_model?path={path}",
                InstancesModelOutput.class,
                platform.getApplicationName(),
                platform.getPlatformName(),
                propertiesPath);
    }

    public void saveGlobalProperties(PlatformIO platform, PropertiesIO propertiesInput) {
        saveProperties(platform, propertiesInput, "#");
    }

    public void saveProperties(PlatformIO platformInput, PropertiesIO propertiesInput, String propertiesPath) {
        saveProperties(platformInput, propertiesInput, propertiesPath, null);
    }

    public void saveProperties(PlatformIO platformInput, PropertiesIO propertiesInput, String propertiesPath, String tryTo) {
        restTemplate.postForEntity(
                "/applications/{application_name}/platforms/{platform_name}/properties?platform_vid={platform_version_id}&path={properties_path}&comment={comment}",
                propertiesInput,
                getResponseType(tryTo, PropertiesIO.class),
                platformInput.getApplicationName(),
                platformInput.getPlatformName(),
                platformInput.getVersionId(),
                propertiesPath,
                "this is a comment");
    }

    public void updateProperties(PlatformIO platformInput, PropertiesIO propertiesInput, String propertiesPath, String tryTo) {
        restTemplate.putForEntity(
                "/applications/{application_name}/platforms/{platform_name}/properties?platform_vid={platform_version_id}&path={properties_path}&comment={comment}",
                propertiesInput,
                getResponseType(tryTo, PropertiesIO.class),
                platformInput.getApplicationName(),
                platformInput.getPlatformName(),
                platformInput.getVersionId(),
                propertiesPath,
                "this is a comment");
    }

    public void getGlobalPropertiesUsage(PlatformIO platform) {
        restTemplate.exchange(
                "/applications/{application_name}/platforms/{platform_name}/global_properties_usage",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Set<GlobalPropertyUsageOutput>>>() {
                },
                platform.getApplicationName(),
                platform.getPlatformName());
    }

    public PropertiesIO getGlobalProperties(PlatformIO platform) {
        return getProperties(platform, "#");
    }

    public PropertiesIO getProperties(PlatformIO platform, String propertiesPath) {
        getProperties(platform, propertiesPath, null, null);
        return testContext.getResponseBody();
    }

    public void getProperties(PlatformIO platform, String propertiesPath, Long timestamp, String tryTo) {
        String url = "/applications/{application_name}/platforms/{platform_name}/properties?path={properties_path}";
        if (timestamp != null) {
            url += "&timestamp=" + timestamp;
        }
        restTemplate.getForEntity(
                url,
                getResponseType(tryTo, PropertiesIO.class),
                platform.getApplicationName(),
                platform.getPlatformName(),
                propertiesPath);
    }

    public void getPropertiesDiff(PlatformIO fromPlatform, String fromPropertiesPath, String fromInstance, PlatformIO toPlatform, String toPropertiesPath, String toInstance, boolean compareStoredValues, Long timestamp, String tryTo) {
        String url = "/applications/{application_name}/platforms/{platform_name}/properties/diff?path={properties_path}" +
                "&instance_name={instance_name}" +
                "&to_application={to_application}" +
                "&to_platform={to_platform}" +
                "&to_path={to_path}" +
                "&to_instance_name={to_instance_name}" +
                "&compare_stored_values={compare_stored_values}";
        if (timestamp != null) {
            url += "&timestamp=" + timestamp;
        }
        restTemplate.getForEntity(
                url,
                getResponseType(tryTo, PropertiesDiffOutput.class),
                fromPlatform.getApplicationName(),
                fromPlatform.getPlatformName(),
                fromPropertiesPath,
                fromInstance,
                toPlatform.getApplicationName(),
                toPlatform.getPlatformName(),
                toPropertiesPath,
                toInstance,
                compareStoredValues);
    }

    public void getPlatformsUsingModule(ModuleIO module) {
        restTemplate.getForEntity(
                "/applications/using_module/{module_name}/{module_version}/{version_type}",
                ModulePlatformsOutput[].class,
                module.getName(),
                module.getVersion(),
                VersionType.fromIsWorkingCopy(module.getIsWorkingCopy()));
    }

    public void restorePlatform(PlatformIO platformInput, String tryTo) {
        restTemplate.exchange(
                "/applications/{application_name}/platforms/{platform_name}/restore",
                HttpMethod.POST,
                null,
                getResponseType(tryTo, PlatformIO.class),
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public void getAllApplications() {
        restTemplate.getForEntity("/applications/platforms", AllApplicationsDetailOutput.class);
    }
}
