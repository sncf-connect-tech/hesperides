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
package org.hesperides.presentation.io.platforms;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;

@Value
public class PropertiesOutput {
    @SerializedName("key_value_properties")
    List<ValorisedPropertyOutput> valorisedPropertyOutputs;
    @SerializedName("iterable_properties")
    List<IterableValorisedPropertyOutput> iterableValorisedPropertyOutputs;

    @Value
    @NonFinal
    public static abstract class AbstractValorisedPropertyOutput {
        String name;
    }

    @Value
    public static class ValorisedPropertyOutput extends AbstractValorisedPropertyOutput {

        String value;

        public ValorisedPropertyOutput(String name, String value) {
            super(name);
            this.value = value;
        }
    }

    @Value
    public static class IterableValorisedPropertyOutput extends AbstractValorisedPropertyOutput {

        @SerializedName("iterable_valorisation_items")
        List<IterablePropertyItemOutput> iterablePropertyItems;

        public IterableValorisedPropertyOutput(String name, List<IterablePropertyItemOutput> iterablePropertyItems) {
            super(name);
            this.iterablePropertyItems = iterablePropertyItems;
        }
    }

    @Value
    public static class IterablePropertyItemOutput {

        String title;
        @SerializedName("values")
        List<AbstractValorisedPropertyOutput> abstractValorisedPropertyOutputs;
    }
}
