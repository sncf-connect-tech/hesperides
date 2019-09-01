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
package org.hesperides.test.bdd.files;

import org.hesperides.core.presentation.io.files.InstanceFileOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.hesperides.test.bdd.commons.HesperidesScenario.getResponseType;

@Component
public class FileClient {

    @Autowired
    private RestTemplate restTemplate;

    public void getFiles(
            String applicationName,
            String platformName,
            String modulePath,
            String moduleName,
            String moduleVersion,
            String instanceName,
            boolean isWorkingCopy,
            boolean simulate,
            String tryTo) {

        restTemplate.getForEntity("/files/applications/{application_name}/platforms/{platform_name}" +
                        "/{module_path}/{module_name}/{module_version}/instances/{instance_name}" +
                        "?isWorkingCopy={is_working_copy}&simulate={simulate}",
                getResponseType(tryTo, InstanceFileOutput[].class),
                applicationName,
                platformName,
                modulePath,
                moduleName,
                moduleVersion,
                instanceName,
                isWorkingCopy,
                simulate);
    }

    public void getFile(
            String applicationName,
            String platformName,
            String modulePath,
            String moduleName,
            String moduleVersion,
            String instanceName,
            String templateName,
            boolean isWorkingCopy,
            String templateNamespace,
            boolean simulate) {

        //https://hesperides-dev:56789/rest/files/applications/TLH/platforms/DEV-XXX/%23ROOT_GROUP%23PARENT_GROUP%23CHILD_GROUP/module-tlh/2/instances/default/techno-template?isWorkingCopy=true&template_namespace=packages%23techno-tlh%231%23WORKINGCOPY&simulate=true

        restTemplate.getForEntity("/files/applications/{application_name}/platforms/{platform_name}" +
                        "/{module_path}/{module_name}/{module_version}/instances/{instance_name}/{template_name}" +
                        "?isWorkingCopy={is_working_copy}&template_namespace={template_namespace}&simulate={simulate}",
                String.class,
                applicationName,
                platformName,
                modulePath,
                moduleName,
                moduleVersion,
                instanceName,
                templateName,
                isWorkingCopy,
                templateNamespace,
                simulate);
    }
}
