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
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class IterablePropertyItemIO {

    String title;

    @SerializedName("values")
    @JsonProperty("values")
    @Valid
    Set<AbstractValuedPropertyIO> abstractValuedProperties;

    public IterablePropertyItemIO(final IterablePropertyItemView iterablePropertyItemView) {
        this.title = iterablePropertyItemView.getTitle();
        final List<ValuedPropertyView> valuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(iterablePropertyItemView.getAbstractValuedPropertyViews(), ValuedPropertyView.class);
        final Set<ValuedPropertyIO> valuedProperties = ValuedPropertyIO.fromValuedPropertyViews(valuedPropertyViews);
        final List<IterableValuedPropertyView> iterableValuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(iterablePropertyItemView.getAbstractValuedPropertyViews(), IterableValuedPropertyView.class);
        final Set<IterableValuedPropertyIO> iterableValuedProperties = IterableValuedPropertyIO.fromIterableValuedPropertyViews(iterableValuedPropertyViews);
        this.abstractValuedProperties = new HashSet<>(valuedProperties);
        this.abstractValuedProperties.addAll(iterableValuedProperties);
    }

    public static List<IterablePropertyItemIO> fromIterablePropertyItem(final List<IterablePropertyItemView> iterablePropertyItems) {
        return Optional.ofNullable(iterablePropertyItems)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterablePropertyItemIO::new)
                .collect(Collectors.toList());
    }

    public IterablePropertyItem toDomainInstance() {
        List<AbstractValuedProperty> abstractValuedProperties = new ArrayList<>();

        // Récupération des valuedPropertyIOS dans la liste d'abstact et transformation en globalProperties du domaine
        List<ValuedPropertyIO> propertyIOS = AbstractValuedPropertyIO.getPropertyWithType(this.abstractValuedProperties, ValuedPropertyIO.class);
        abstractValuedProperties.addAll(ValuedPropertyIO.toDomainInstances(new HashSet<>(propertyIOS)));

        // Récupération des iterableValuedPropertyIOS dans la liste d'abstact et transformation en iterableValuedProperties du domaine
        List<IterableValuedPropertyIO> iterableValuedPropertyIOS = AbstractValuedPropertyIO.getPropertyWithType(this.abstractValuedProperties, IterableValuedPropertyIO.class);
        abstractValuedProperties.addAll(IterableValuedPropertyIO.toDomainInstances(new HashSet<>(iterableValuedPropertyIOS)));

        return new IterablePropertyItem(title, abstractValuedProperties);
    }
}
