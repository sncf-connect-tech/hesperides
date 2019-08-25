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
package org.hesperides.test.bdd.applications;

import org.hesperides.core.presentation.io.platforms.AllApplicationsDetailOutput;
import org.hesperides.core.presentation.io.platforms.ApplicationDirectoryGroupsInput;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.SearchResultOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.hesperides.test.bdd.commons.HesperidesScenario.getResponseType;

@Component
public class ApplicationClient {

    private final RestTemplate restTemplate;

    @Autowired
    public ApplicationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void getApplications(String tryTo) {
        restTemplate.getForEntity("/applications", getResponseType(tryTo, SearchResultOutput[].class));
    }

    public void getApplication(String applicationName, boolean hidePlatform, boolean withPasswordFlag, String tryTo) {
        restTemplate.getForEntity(
                "/applications/{application_name}?hide_platform={hide_platform}&with_password_info={with_password_flag}",
                getResponseType(tryTo, ApplicationOutput.class),
                applicationName,
                hidePlatform,
                withPasswordFlag);
    }

    public void getAllApplicationsDetail(boolean withPasswordFlag) {
        restTemplate.getForEntity("/applications/platforms?with_password_info={with_password_flag}",
                AllApplicationsDetailOutput.class, withPasswordFlag);
    }

    public void setApplicationDirectoryGroups(String applicationName, ApplicationDirectoryGroupsInput applicationDirectoryGroups) {
        //TODO utiliser customtemplate.putForEntity
        restTemplate.exchange("/applications/{application_name}/directory_groups",
                HttpMethod.PUT,
                new HttpEntity<>(applicationDirectoryGroups),
                String.class,
                applicationName);
    }
}
