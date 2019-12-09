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
package org.hesperides.core.domain.platforms.queries.views.properties;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView.toDomainAbstractValuedProperties;

import static java.util.stream.Collectors.toList;

@Value
public class IterablePropertyItemView {

    String title;
    List<AbstractValuedPropertyView> abstractValuedPropertyViews;

    public IterablePropertyItem toDomainIterablePropertyItem() {
        return new IterablePropertyItem(title, toDomainAbstractValuedProperties(abstractValuedPropertyViews));
    }

    public IterablePropertyItemView withPasswordsHidden(Predicate<String> isPassword) {
        return new IterablePropertyItemView(title, abstractValuedPropertyViews.stream()
                .map(property -> property.withPasswordsHidden(isPassword))
                .collect(toList()));
    }

    public static List<IterablePropertyItem> toDomainIterablePropertyItems(List<IterablePropertyItemView> iterablePropertyItems) {
        return Optional.ofNullable(iterablePropertyItems)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(IterablePropertyItemView::toDomainIterablePropertyItem)
                .collect(toList());
    }

    IterablePropertyItemView excludePropertyWithOnlyDefaultValue(List<AbstractPropertyView> propertiesModel) {
        return new IterablePropertyItemView(title, AbstractValuedPropertyView.excludePropertiesWithOnlyDefaultValue(abstractValuedPropertyViews, propertiesModel));
    }

    IterablePropertyItemView excludeUnusedValues(List<AbstractPropertyView> propertiesModel, Set<String> indirections) {
        final List<AbstractValuedPropertyView> surviving = AbstractValuedPropertyView.excludeUnusedValues(
                abstractValuedPropertyViews, propertiesModel, indirections).collect(toList());

        return new IterablePropertyItemView(title, surviving);
    }
}
