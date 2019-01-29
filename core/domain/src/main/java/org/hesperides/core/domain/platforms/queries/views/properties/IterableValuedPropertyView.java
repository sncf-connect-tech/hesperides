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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.IterablePropertyItem;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView.toDomainIterablePropertyItems;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterableValuedPropertyView extends AbstractValuedPropertyView {

    List<IterablePropertyItemView> iterablePropertyItems;

    public IterableValuedPropertyView(String name, List<IterablePropertyItemView> iterablePropertyItems) {
        super(name);
        this.iterablePropertyItems = iterablePropertyItems;
    }

    @Override
    public AbstractValuedPropertyView withPasswordsHidden() {
        return new IterableValuedPropertyView(getName(), iterablePropertyItems.stream()
                .map(IterablePropertyItemView::withPasswordsHidden)
                .collect(Collectors.toList()));
    }

    @Override
    public IterableValuedProperty toDomainValuedProperty() {
        return new IterableValuedProperty(getName(), toDomainIterablePropertyItems(iterablePropertyItems));
    }
}
