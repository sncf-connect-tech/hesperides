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

import static org.hesperides.core.domain.platforms.queries.views.properties.IterablePropertyItemView.toDomainIterablePropertyItems;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.core.domain.templatecontainers.queries.IterablePropertyView;

import lombok.EqualsAndHashCode;
import lombok.Value;

import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    protected Optional<AbstractValuedPropertyView> excludePropertyWithOnlyDefaultValue(Map<String, AbstractPropertyView> modelPerName) {
        List<AbstractPropertyView> propertiesModel = findPropertiesModel(modelPerName);

        List<IterablePropertyItemView> items = iterablePropertyItems.stream()
                .map(item -> item.excludePropertyWithOnlyDefaultValue(propertiesModel))
                .collect(Collectors.toList());

        return Optional.of(new IterableValuedPropertyView(getName(), items));
    }

    @Override
    protected Optional<AbstractValuedPropertyView> excludeUnusedValue(
            Map<String, AbstractPropertyView> propertiesPerName, Set<String> referencedProperties) {
        List<AbstractPropertyView> propertiesModel = findPropertiesModel(propertiesPerName);

        List<IterablePropertyItemView> survivingItems = iterablePropertyItems.stream()
                .map(item -> item.excludeUnusedValues(propertiesModel, referencedProperties))
                .collect(Collectors.toList());

        return Optional.of(new IterableValuedPropertyView(getName(), survivingItems));
    }

    private List<AbstractPropertyView> findPropertiesModel(Map<String, AbstractPropertyView> propertiesPerName) {
        final List<AbstractPropertyView> properties;
        final AbstractPropertyView property = propertiesPerName.get(getName());

        if (property instanceof IterablePropertyView) {
            properties = ((IterablePropertyView) property).getProperties();
        } else {
            properties = emptyList();
        }

        return properties;
    }

    @Override
    public IterableValuedProperty toDomainValuedProperty() {
        return new IterableValuedProperty(getName(), toDomainIterablePropertyItems(iterablePropertyItems));
    }
}
