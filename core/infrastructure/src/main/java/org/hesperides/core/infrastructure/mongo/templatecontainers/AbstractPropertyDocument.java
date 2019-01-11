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
package org.hesperides.core.infrastructure.mongo.templatecontainers;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.entities.IterableProperty;
import org.hesperides.core.domain.templatecontainers.entities.Property;
import org.hesperides.core.domain.templatecontainers.queries.AbstractPropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public abstract class AbstractPropertyDocument {

    protected String name;

    public static List<AbstractPropertyDocument> fromDomainInstances(List<AbstractProperty> properties) {
        return Optional.ofNullable(properties)
                .orElse(Collections.emptyList())
                .stream()
                .map(property -> property instanceof Property
                        ? new PropertyDocument((Property) property)
                        : new IterablePropertyDocument((IterableProperty) property)
                ).collect(Collectors.toList());
    }

    public static List<AbstractPropertyView> toViews(List<AbstractPropertyDocument> properties) {
        return Optional.ofNullable(properties)
                .orElse(Collections.emptyList())
                .stream()
                .map(AbstractPropertyDocument::toView)
                .collect(Collectors.toList());
    }

    public abstract AbstractPropertyView toView();
}
