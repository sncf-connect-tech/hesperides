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
package org.hesperides.core.domain.files;

import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Value
public class InstanceFileView {

    String name;
    String filename;
    String location;
    String url;
    TemplateView.RightsView rights;

    public InstanceFileView(String templateLocation,
                            String templateFilename,
                            PlatformView platform,
                            String modulePath,
                            Module.Key moduleKey,
                            String instanceName,
                            TemplateView template,
                            boolean simulate) {

        name = template.getName();
        filename = template.getFilename();
        location = buildLegacyFileLocation(templateLocation, templateFilename);
        url = buildUrl(platform.getApplicationName(),
                platform.getPlatformName(),
                modulePath,
                moduleKey.getName(),
                moduleKey.getVersion(),
                moduleKey.isWorkingCopy(),
                instanceName,
                template.getName(),
                template.getNamespace(),
                simulate);
        rights = template.getRights();
    }

    static String buildLegacyFileLocation(String location, String filename) {
        return location + "/" + filename;
    }

    private String buildUrl(final String applicationName,
                            final String platformName,
                            final String path,
                            final String moduleName,
                            final String moduleVersion,
                            final boolean isWorkingCopy,
                            final String instanceName,
                            final String fileName,
                            final String templateNamespace,
                            final boolean simulate) {
        try {
            return String.format("/rest/applications/%1$s/platforms/%2$s/%3$s/%4$s/%5$s/instances/%6$s/files/%7$s?isWorkingCopy=%8$s&template_namespace=%9$s&simulate=%10$s",
                    URLEncoder.encode(applicationName, "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(platformName, "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(path, "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(moduleName, "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(moduleVersion, "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(instanceName, "UTF-8").replace("+", "%20"),
                    URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"),
                    isWorkingCopy,
                    URLEncoder.encode(templateNamespace, "UTF-8").replace("+", "%20"),
                    simulate
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
