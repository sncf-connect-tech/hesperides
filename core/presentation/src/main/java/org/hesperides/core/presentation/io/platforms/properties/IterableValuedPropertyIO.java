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
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterableValuedPropertyIO extends AbstractValuedPropertyIO {

    @SerializedName("iterable_valorisation_items")
    List<IterablePropertyItemIO> iterablePropertyItems;

    public IterableValuedPropertyIO(String name, List<IterablePropertyItemIO> iterablePropertyItems) {
        super(name);
        this.iterablePropertyItems = iterablePropertyItems;
    }

    public IterableValuedPropertyIO(IterableValuedPropertyView iterableValuedPropertyView) {
        super(iterableValuedPropertyView.getName());
        this.iterablePropertyItems = IterablePropertyItemIO.fromIterablePropertyItem(iterableValuedPropertyView.getIterablePropertyItems());
    }

    public static List<IterableValuedPropertyIO> fromIterableValuedPropertyViews(final List<IterableValuedPropertyView> iterableValuedPropertyViews) {
        return Optional.ofNullable(iterableValuedPropertyViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterableValuedPropertyIO::new)
                .collect(Collectors.toList());
    }

    public IterableValuedProperty toDomainInstance() {
        List<IterablePropertyItem> iterablePropertyItems = Optional.ofNullable(this.iterablePropertyItems)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterablePropertyItemIO::toDomainInstance)
                .collect(Collectors.toList());
        return new IterableValuedProperty(getName(), iterablePropertyItems);
    }

    public static List<IterableValuedProperty> toDomainInstances(List<IterableValuedPropertyIO> iterableValuedPropertyIOS) {
        return Optional.ofNullable(iterableValuedPropertyIOS)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterableValuedPropertyIO::toDomainInstance)
                .collect(Collectors.toList());
    }
}
