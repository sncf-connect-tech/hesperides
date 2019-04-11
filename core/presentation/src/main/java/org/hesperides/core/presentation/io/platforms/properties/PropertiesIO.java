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
package org.hesperides.core.presentation.io.platforms.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Value
@AllArgsConstructor
public class PropertiesIO {

    @NotNull
    @SerializedName("key_value_properties")
    @JsonProperty("key_value_properties")
    @Valid
    Set<ValuedPropertyIO> valuedProperties;

    @NotNull
    @SerializedName("iterable_properties")
    @JsonProperty("iterable_properties")
    Set<IterableValuedPropertyIO> iterableValuedProperties;

    public List<AbstractValuedProperty> toDomainInstances() {
        final List<ValuedProperty> valuedProperties = ValuedPropertyIO.toDomainInstances(this.valuedProperties);
        final List<IterableValuedProperty> iterableValuedProperties = IterableValuedPropertyIO.toDomainInstances(this.iterableValuedProperties);
        final List<AbstractValuedProperty> properties = new ArrayList<>();
        properties.addAll(valuedProperties);
        properties.addAll(iterableValuedProperties);
        return properties;
    }

    public PropertiesIO(List<AbstractValuedPropertyView> abstractValuedPropertyViews) {

        final List<ValuedPropertyView> valuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, ValuedPropertyView.class);
        valuedProperties = ValuedPropertyIO.fromValuedPropertyViews(valuedPropertyViews);

        final List<IterableValuedPropertyView> iterableValuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(abstractValuedPropertyViews, IterableValuedPropertyView.class);
        iterableValuedProperties = IterableValuedPropertyIO.fromIterableValuedPropertyViews(iterableValuedPropertyViews);
    }
}
