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
package org.hesperides.tests.bddrefacto.platforms;

import org.hesperides.core.presentation.io.platforms.PlatformInput;
import org.hesperides.core.presentation.io.platforms.PlatformOutput;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PlatformClient {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity create(PlatformInput platformInput) {
        return create(platformInput, PlatformOutput.class);
    }

    public ResponseEntity create(PlatformInput platformInput, Class responseType) {
        return restTemplate.postForEntity(
                "/applications/{application_name}/platforms",
                platformInput,
                responseType,
                platformInput.getApplicationName());
    }

    public ResponseEntity delete(PlatformInput platformInput, Class responseType) {
        return restTemplate.exchange(
                "/applications/{application_name}/platforms/{platform_name}",
                HttpMethod.DELETE,
                null,
                responseType,
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public ResponseEntity getApplication(PlatformInput platformInput, Class responseType) {
        return restTemplate.getForEntity(
                "/applications/{application_name}",
                responseType,
                platformInput.getApplicationName());
    }

    public ResponseEntity get(PlatformInput platformInput, Class responseType) {
        return restTemplate.getForEntity(
                "/applications/{application_name}/platforms/{platform_name}",
                responseType,
                platformInput.getApplicationName(),
                platformInput.getPlatformName());
    }

    public ResponseEntity update(PlatformInput platformInput, boolean copyProperties) {
        String url = "/applications/{application_name}/platforms";
        if (copyProperties) {
            url += "?copyPropertiesForUpgradedModules=true";
        }
        return restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(platformInput),
                PlatformOutput.class,
                platformInput.getApplicationName());
    }

    public ResponseEntity<SearchResultOutput[]> searchApplication(String search) {
        return restTemplate.postForEntity("/applications/perform_search?name=" + search, null, SearchResultOutput[].class);
    }

    public ResponseEntity<SearchResultOutput[]> search(String applicationName) {
        return search(applicationName, null);
    }

    public ResponseEntity<SearchResultOutput[]> search(String applicationName, String platformName) {
        String url = "/applications/platforms/perform_search?applicationName=" + applicationName;
        if (platformName != null) {
            url += "&platformName=" + platformName;
        }
        return restTemplate.postForEntity(
                url,
                null,
                SearchResultOutput[].class);
    }
}
