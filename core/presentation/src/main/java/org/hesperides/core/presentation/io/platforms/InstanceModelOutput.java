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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.InstancePropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class InstanceModelOutput {

    @SerializedName("keys")
    @JsonProperty("keys")
    List<InstancePropertyOutput> instanceProperties;

    public static InstanceModelOutput fromInstancePropertyViews(List<InstancePropertyView> instancePropertyViews) {
        return new InstanceModelOutput(Optional.ofNullable(instancePropertyViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(InstancePropertyOutput::new)
                .collect(Collectors.toList()));
    }

    @Value
    @AllArgsConstructor
    public static class InstancePropertyOutput {

        String name;
        String comment;
        @SerializedName("required")
        @JsonProperty("required")
        Boolean isRequired;
        String defaultValue;
        String pattern;
        @SerializedName("password")
        @JsonProperty("password")
        Boolean isPassword;

        public InstancePropertyOutput(InstancePropertyView instancePropertyView) {
            name = instancePropertyView.getName();
            comment = "";
            isRequired = false;
            defaultValue = "";
            pattern = "";
            isPassword = false;
        }
    }
}
