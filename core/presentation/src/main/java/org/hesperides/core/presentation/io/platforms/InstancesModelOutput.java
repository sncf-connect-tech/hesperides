/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class InstancesModelOutput {

    @SerializedName("keys")
    @JsonProperty("keys")
    Set<InstancePropertyOutput> instanceProperties;

    public static InstancesModelOutput fromInstancesModelView(List<String> instancesModelView) {
        return new InstancesModelOutput(Optional.ofNullable(instancesModelView)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(InstancePropertyOutput::new)
                .collect(Collectors.toSet()));
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

        public InstancePropertyOutput(String instancePropertyName) {
            name = instancePropertyName;
            comment = "";
            isRequired = false;
            defaultValue = null;
            pattern = null;
            isPassword = false;
        }
    }
}
