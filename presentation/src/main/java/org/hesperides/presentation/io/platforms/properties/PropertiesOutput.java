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
package org.hesperides.presentation.io.platforms.properties;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.util.List;

@Value
public class PropertiesOutput {
    @SerializedName("key_value_properties")
    List<ValuedPropertyIO> valuedPropertyOutputs;
    @SerializedName("iterable_properties")
    List<IterableValuedPropertyIO> iterableValuedPropertyOutputs;

}
