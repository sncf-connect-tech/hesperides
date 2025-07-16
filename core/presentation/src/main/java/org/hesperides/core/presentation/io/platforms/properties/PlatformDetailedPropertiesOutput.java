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
package org.hesperides.core.presentation.io.platforms.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformDetailedPropertiesView;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformDetailedPropertiesView.DetailedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformDetailedPropertiesView.ModuleDetailedPropertyView;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@Value
public class PlatformDetailedPropertiesOutput {

    @SerializedName("application_name")
    @JsonProperty("application_name")
    String applicationName;
    @SerializedName("platform_name")
    @JsonProperty("platform_name")
    String platformName;
    @SerializedName("global_properties")
    @JsonProperty("global_properties")
    List<DetailedPropertyOutput> globalProperties;
    @SerializedName("detailed_properties")
    @JsonProperty("detailed_properties")
    List<ModuleDetailedPropertyOutput> detailedProperties;

    public PlatformDetailedPropertiesOutput(PlatformDetailedPropertiesView view) {
        applicationName = view.getApplicationName();
        platformName = view.getPlatformName();
        globalProperties = DetailedPropertyOutput.fromDetailedPropertyViews(view.getGlobalProperties());
        detailedProperties = ModuleDetailedPropertyOutput.fromModuleDetailedPropertyViews(view.getDetailedProperties());
    }

    @Value
    @NonFinal
    @AllArgsConstructor
    public static class DetailedPropertyOutput {
        String name;
        @SerializedName("stored_value")
        @JsonProperty("stored_value")
        String storedValue;
        @SerializedName("final_value")
        @JsonProperty("final_value")
        String finalValue;

        public DetailedPropertyOutput(DetailedPropertyView view) {
            name = view.getName();
            storedValue = view.getStoredValue();
            finalValue = view.getFinalValue();
        }

        public static List<DetailedPropertyOutput> fromDetailedPropertyViews(List<DetailedPropertyView> views) {
            return views.stream()
                    .map(DetailedPropertyOutput::new)
                    .collect(toList());
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class ModuleDetailedPropertyOutput extends DetailedPropertyOutput {
        @SerializedName("default_value")
        @JsonProperty("default_value")
        String defaultValue;
        @SerializedName("is_required")
        @JsonProperty("is_required")
        boolean isRequired;
        @SerializedName("is_password")
        @JsonProperty("is_password")
        boolean isPassword;
        String pattern;
        String comment;
        @SerializedName("properties_path")
        @JsonProperty("properties_path")
        String propertiesPath;
        @SerializedName("referenced_global_properties")
        @JsonProperty("referenced_global_properties")
        List<DetailedPropertyOutput> referencedGlobalProperties;
        @SerializedName("is_unused")
        @JsonProperty("is_unused")
        boolean isUnused;

        public ModuleDetailedPropertyOutput(
                String name,
                String storedValue,
                String finalValue,
                String defaultValue,
                boolean isRequired,
                boolean isPassword,
                String pattern,
                String comment,
                String propertiesPath,
                List<DetailedPropertyOutput> referencedGlobalProperties,
                boolean isUnused) {
            super(name, storedValue, finalValue);
            this.defaultValue = defaultValue;
            this.isRequired = isRequired;
            this.isPassword = isPassword;
            this.pattern = pattern;
            this.comment = comment;
            this.propertiesPath = propertiesPath;
            this.referencedGlobalProperties = referencedGlobalProperties;
            this.isUnused = isUnused;
        }

        public ModuleDetailedPropertyOutput(ModuleDetailedPropertyView view) {
            super(view.getName(), view.getStoredValue(), view.getFinalValue());
            defaultValue = defaultIfEmpty(view.getDefaultValue(), null);
            isRequired = view.isRequired();
            isPassword = view.isPassword();
            pattern = defaultIfEmpty(view.getPattern(), null);
            comment = defaultIfEmpty(view.getComment(), null);
            propertiesPath = view.getPropertiesPath();
            referencedGlobalProperties = DetailedPropertyOutput.fromDetailedPropertyViews(view.getReferencedGlobalProperties());
            isUnused = view.isUnused();
        }

        public static List<ModuleDetailedPropertyOutput> fromModuleDetailedPropertyViews(List<ModuleDetailedPropertyView> views) {
            return views.stream()
                    .map(ModuleDetailedPropertyOutput::new)
                    .collect(toList());
        }
    }
}
