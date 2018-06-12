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
import org.hesperides.domain.templatecontainer.queries.ModelView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class ModelOutput {

    @SerializedName("key_value_properties")
    List<PropertyOutput> properties;
    @SerializedName("iterable_properties")
    List<IterablePropertyOutput> iterableProperties;

    @Value
    public static class PropertyOutput {

        String name;
        @SerializedName("required")
        boolean isRequired;
        String comment;
        String defaultValue;
        String pattern;
        @SerializedName("password")
        boolean isPassword;

        public static List<PropertyOutput> fromViews(List<ModelView.PropertyView> propertyViews) {
            List<PropertyOutput> propertyOutputs = null;
            if (propertyViews != null) {
                propertyOutputs = propertyViews.stream().map(PropertyOutput::fromView).collect(Collectors.toList());
            }
            return propertyOutputs;
        }

        public static PropertyOutput fromView(ModelView.PropertyView propertyView) {
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
    public static class IterablePropertyOutput {

        String name;
        PropertyOutput propertyOutput;

        public static List<IterablePropertyOutput> fromViews(List<ModelView.IterablePropertyView> iterablePropertyViews) {
            List<IterablePropertyOutput> iterablePropertyOutputs = null;
            if (iterablePropertyViews != null) {
                iterablePropertyOutputs = iterablePropertyViews.stream().map(IterablePropertyOutput::fromView).collect(Collectors.toList());
            }
            return iterablePropertyOutputs;
        }

        public static IterablePropertyOutput fromView(ModelView.IterablePropertyView iterablePropertyView) {
            IterablePropertyOutput iterablePropertyOutput = null;
            if (iterablePropertyView != null) {
                iterablePropertyOutput = new IterablePropertyOutput(iterablePropertyView.getName(), PropertyOutput.fromView(iterablePropertyView.getProperty()));
            }
            return iterablePropertyOutput;
        }
    }

    public static ModelOutput fromView(ModelView modelView) {
        ModelOutput modelOutput = null;
        if (modelView != null) {
            modelOutput = new ModelOutput(
                    PropertyOutput.fromViews(modelView.getProperties()),
                    IterablePropertyOutput.fromViews(modelView.getIterableProperties())
            );
        }
        return modelOutput;
    }
}
