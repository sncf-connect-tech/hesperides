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
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;

@Value
public class InstanceFileView {

    String location;
    String url;
    Rights rights;

    public InstanceFileView(Platform.Key platformKey, String path, Module.Key moduleKey, String instanceName, TemplateView template, boolean simulate) {
        location = buildFileLocation(template.getLocation(), template.getFilename());
        url = "/rest/files/applications/" + platformKey.getApplicationName() +
                "/platforms/" + platformKey.getPlatformName()
                + "/" + path
                + "/" + moduleKey.getName()
                + "/" + moduleKey.getVersion()
                + "/instances/" + instanceName
                + "/" + template.getName()
                + "?isWorkingcopy=" + moduleKey.isWorkingCopy()
                + "&template_namespace=" + template.getNamespace()
                + "&simulate=" + simulate;
        rights = new Rights(template.getRights());
    }

    private String buildFileLocation(String location, String filename) {
        StringBuilder fileLocation = new StringBuilder();
        if (location != null) {
            fileLocation.append(location);
            if (!location.endsWith("/")) {
                fileLocation.append("/");
            }
        }
        fileLocation.append(fileLocation);
        return fileLocation.toString();
    }

    @Value
    public static class Rights {

        String user;
        String group;
        String other;

        public Rights(TemplateView.RightsView rights) {
            user = printFileRights(rights.getUser());
            group = printFileRights(rights.getGroup());
            other = printFileRights(rights.getOther());
        }

        private String printFileRights(TemplateView.FileRightsView fileRights) {
            return fileRights != null ? fileRights.toString() : "";
        }
    }
}
