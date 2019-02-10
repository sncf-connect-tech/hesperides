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
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    protected Stream<ValuedPropertyView> flattenProperties() {
        return Optional.ofNullable(iterablePropertyItems)
                .orElse(Collections.emptyList())
                .stream()
                .map(IterablePropertyItemView::getAbstractValuedPropertyViews)
                .flatMap(List::stream)
                .map(AbstractValuedPropertyView::flattenProperties)
                .flatMap(Function.identity());
    }

    @Override
    public AbstractValuedPropertyView withPasswordsHidden() {
        return new IterableValuedPropertyView(getName(), iterablePropertyItems.stream()
                .map(IterablePropertyItemView::withPasswordsHidden)
                .collect(Collectors.toList()));
    }

    @Override
    protected Optional<AbstractValuedPropertyView> getOnlyValuedProperty() {

        List<IterablePropertyItemView> items = iterablePropertyItems.stream()
                .map(IterablePropertyItemView::withOnlyValuedProperty)
                .collect(Collectors.toList());

        return Optional.of(new IterableValuedPropertyView(getName(), items));
    }

    @Override
    public IterableValuedProperty toDomainValuedProperty() {
        return new IterableValuedProperty(getName(), toDomainIterablePropertyItems(iterablePropertyItems));
    }
}
