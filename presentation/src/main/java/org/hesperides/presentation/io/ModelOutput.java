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
package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.domain.templatecontainer.queries.ModelView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class ModelOutput {

    @SerializedName("key_value_properties")
    List<PropertyOutput> properties;
    @SerializedName("iterable_properties")
    List<IterablePropertyOutput> iterableProperties;

    public static ModelOutput fromView(ModelView modelView) {
        ModelOutput modelOutput = null;
        if (modelView != null) {
            modelOutput = new ModelOutput(
                    PropertyOutput.fromPropertyViews(modelView.getProperties()),
                    IterablePropertyOutput.fromIterablePropertyViews(modelView.getIterableProperties())
            );
        }
        return modelOutput;
    }

    @Value
    @NonFinal
    public static class PropertyOutput {

        String name;
        @SerializedName("required")
        boolean isRequired;
        String comment;
        String defaultValue;
        String pattern;
        @SerializedName("password")
        boolean isPassword;

        public static List<PropertyOutput> fromPropertyViews(List<ModelView.PropertyView> propertyViews) {
            List<PropertyOutput> propertyOutputs = null;
            if (propertyViews != null) {
                propertyOutputs = propertyViews.stream().map(PropertyOutput::fromPropertyView).collect(Collectors.toList());
            }
            return propertyOutputs;
        }

        public static PropertyOutput fromPropertyView(ModelView.PropertyView propertyView) {
            PropertyOutput propertyOutput = null;
            if (propertyView != null) {
                propertyOutput = new PropertyOutput(
                        propertyView.getName(),
                        propertyView.isRequired(),
                        propertyView.getComment(),
                        propertyView.getDefaultValue(),
                        propertyView.getPattern(),
                        propertyView.isPassword()
                );
            }
            return propertyOutput;
        }
    }

    @Value
    public static class IterablePropertyOutput extends PropertyOutput {

        @SerializedName("fields")
        List<PropertyOutput> properties;

        public IterablePropertyOutput(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword, List<PropertyOutput> properties) {
            super(name, isRequired, comment, defaultValue, pattern, isPassword);
            this.properties = properties;
        }

        public static List<IterablePropertyOutput> fromIterablePropertyViews(List<ModelView.IterablePropertyView> iterablePropertyViews) {
            List<IterablePropertyOutput> iterablePropertyOutputs = null;
            if (iterablePropertyViews != null) {
                iterablePropertyOutputs = iterablePropertyViews.stream().map(IterablePropertyOutput::fromIterablePropertyView).collect(Collectors.toList());
            }
            return iterablePropertyOutputs;
        }

        public static IterablePropertyOutput fromIterablePropertyView(ModelView.IterablePropertyView iterablePropertyView) {
            IterablePropertyOutput iterablePropertyOutput = null;
            if (iterablePropertyView != null) {
                iterablePropertyOutput = new IterablePropertyOutput(
                        iterablePropertyView.getName(),
                        iterablePropertyView.isRequired(),
                        iterablePropertyView.getComment(),
                        iterablePropertyView.getDefaultValue(),
                        iterablePropertyView.getPattern(),
                        iterablePropertyView.isPassword(),
                        PropertyOutput.fromPropertyViews(iterablePropertyView.getProperties())
                );
            }
            return iterablePropertyOutput;
        }
    }
}
