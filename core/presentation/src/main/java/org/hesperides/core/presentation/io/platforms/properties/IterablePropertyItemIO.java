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

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
public class IterablePropertyItemIO {

    String title;
    @SerializedName("values")
    List<AbstractValuedPropertyIO> abstractValuedPropertyIOS;

    public IterablePropertyItemIO(final IterablePropertyItemView iterablePropertyItemView) {
        this.title = iterablePropertyItemView.getTitle();
        final List<ValuedPropertyView> valuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(iterablePropertyItemView.getAbstractValuedPropertyViews(), ValuedPropertyView.class);
        final List<ValuedPropertyIO> valuedPropertyIOS = ValuedPropertyIO.fromValuedPropertyViews(valuedPropertyViews);
        final List<IterableValuedPropertyView> iterableValuedPropertyViews = AbstractValuedPropertyView.getAbstractValuedPropertyViewWithType(iterablePropertyItemView.getAbstractValuedPropertyViews(), IterableValuedPropertyView.class);
        final List<IterableValuedPropertyIO> iterableValuedPropertyIOS = IterableValuedPropertyIO.fromIterableValuedPropertyViews(iterableValuedPropertyViews);
        this.abstractValuedPropertyIOS = new ArrayList<>(valuedPropertyIOS);
        this.abstractValuedPropertyIOS.addAll(iterableValuedPropertyIOS);
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

        // Récupération des valuedPropertyIOS dans la liste d'abstact et transformation en valuedProperties du domaine
        List<ValuedPropertyIO> propertyIOS = AbstractValuedPropertyIO.getPropertyWithType(abstractValuedPropertyIOS, ValuedPropertyIO.class);
        abstractValuedProperties.addAll(ValuedPropertyIO.toDomainInstances(propertyIOS));

        // Récupération des iterableValuedPropertyIOS dans la liste d'abstact et transformation en iterableValuedProperties du domaine
        List<IterableValuedPropertyIO> iterableValuedPropertyIOS = AbstractValuedPropertyIO.getPropertyWithType(abstractValuedPropertyIOS, IterableValuedPropertyIO.class);
        abstractValuedProperties.addAll(IterableValuedPropertyIO.toDomainInstances(iterableValuedPropertyIOS));

        return new IterablePropertyItem(title, abstractValuedProperties);
    }
}
