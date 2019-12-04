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
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
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
    public AbstractValuedPropertyView withPasswordsHidden(Predicate<String> isPassword) {
        return new IterableValuedPropertyView(getName(), iterablePropertyItems.stream()
                .map(property -> property.withPasswordsHidden(isPassword))
                .collect(Collectors.toList()));
    }

    @Override
    protected Optional<AbstractValuedPropertyView> excludePropertyWithOnlyDefaultValue(Function<String, AbstractPropertyView> modelFinder) {
        return Optional.of(excluding(IterablePropertyItemView::excludePropertyWithOnlyDefaultValue, modelFinder));
    }

    @Override
    protected Optional<AbstractValuedPropertyView> excludePropertyOutsideModel(Function<String, AbstractPropertyView> modelFinder) {
        return Optional.of(excluding(IterablePropertyItemView::excludePropertyOutsideModel, modelFinder));
    }

    private IterableValuedPropertyView excluding(
            BiFunction<IterablePropertyItemView, List<AbstractPropertyView>, IterablePropertyItemView> sanitizer,
            Function<String, AbstractPropertyView> modelFinder) {

        List<AbstractPropertyView> propertiesModel = findPropertiesModel(modelFinder);

        List<IterablePropertyItemView> items = iterablePropertyItems.stream()
                .map(item -> sanitizer.apply(item, propertiesModel))
                .collect(Collectors.toList());

        return new IterableValuedPropertyView(getName(), items);
    }

    private List<AbstractPropertyView> findPropertiesModel(Function<String, AbstractPropertyView> modelFinder) {
        return Optional.ofNullable(modelFinder.apply(getName()))
                .filter(IterablePropertyView.class::isInstance)
                .map(view -> ((IterablePropertyView) view).getProperties())
                .orElse(null);
    }

    @Override
    public IterableValuedProperty toDomainValuedProperty() {
        return new IterableValuedProperty(getName(), toDomainIterablePropertyItems(iterablePropertyItems));
    }
}
