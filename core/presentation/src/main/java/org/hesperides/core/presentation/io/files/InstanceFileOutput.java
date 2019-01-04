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
package org.hesperides.core.presentation.io.files;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.files.InstanceFileView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;

import java.util.Optional;

@Value
@AllArgsConstructor
public class InstanceFileOutput {

    String location;
    String url;
    Rights rights;

    public InstanceFileOutput(InstanceFileView instanceFileView) {
        location = instanceFileView.getLocation();
        url = instanceFileView.getUrl();
        rights = Optional.ofNullable(instanceFileView.getRights()).map(Rights::new).orElse(null);
    }

    @Value
    @AllArgsConstructor
    public static class Rights {

        String user;
        String group;
        String other;

        public Rights(TemplateView.RightsView rights) {
            user = Optional.ofNullable(rights).map(TemplateView.RightsView::getUser).map(Rights::fileRightsToString).orElse("");
            group = Optional.ofNullable(rights).map(TemplateView.RightsView::getGroup).map(Rights::fileRightsToString).orElse("");
            other = Optional.ofNullable(rights).map(TemplateView.RightsView::getOther).map(Rights::fileRightsToString).orElse("");
        }

        private static String fileRightsToString(TemplateView.FileRightsView fileRights) {
            return new StringBuilder()
                    .append(booleanToString(fileRights.getRead(), "r"))
                    .append(booleanToString(fileRights.getWrite(), "w"))
                    .append(booleanToString(fileRights.getExecute(), "x"))
                    .toString();
        }

        private static String booleanToString(Boolean value, String valueIfTrue) {
            String string = "";
            if (value == null) {
                string = " ";
            } else if (value) {
                string = valueIfTrue;
            } else {
                string = "-";
            }
            return string;
        }
    }
}
