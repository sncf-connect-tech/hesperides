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
package org.hesperides.core.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.InstanceView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class InstanceModelOutput {

    @SerializedName("keys")
    List<InstancePropertyOutput> instanceProperties;

    @Value
    public static class InstancePropertyOutput {
        String name;
        String comment;
        @SerializedName("required")
        boolean isRequired;
        String defaultValue;
        String pattern;
        @SerializedName("password")
        boolean isPassword;


        public static InstancePropertyOutput fromKeyProperty(String name) {
            return new InstancePropertyOutput(name, "",false, null, null, false);
        }
    }

    public static InstanceModelOutput fromInstanceView(InstanceView instanceView) {
        List<InstancePropertyOutput> instancePropertyOutputs = instanceView.getValuedProperties().stream()
                .map(valuedPropertyView -> InstancePropertyOutput.fromKeyProperty(valuedPropertyView.getName()))
                .collect(Collectors.toList());
        return new InstanceModelOutput(instancePropertyOutputs);
    }
}
