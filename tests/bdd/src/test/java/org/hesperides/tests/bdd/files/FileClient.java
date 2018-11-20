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
package org.hesperides.tests.bdd.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FileClient {

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity getInstanceFiles(
            String applicationName,
            String platformName,
            String path,
            String moduleName,
            String moduleVersion,
            String instanceName,
            boolean isWorkingCopy,
            boolean simulate,
            Class responseType) {

        return restTemplate.getForEntity("/files/applications/{application_name}/platforms/{platform_name}" +
                        "/{path}/{module_name}/{module_version}/instances/{instance_name}" +
                        "?isWorkingCopy={is_working_copy}&simulate={simulate}",
                responseType,
                applicationName,
                platformName,
                path,
                moduleName,
                moduleVersion,
                instanceName,
                isWorkingCopy,
                simulate);
    }
}
