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
package org.hesperides.core.infrastructure.mongo.platforms.documents;

import lombok.Data;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public abstract class AbstractValuedPropertyDocument {

    protected String name;

    public static List<AbstractValuedPropertyView> toAbstractValuedPropertyViews(final List<AbstractValuedPropertyDocument> properties) {

        return Optional.ofNullable(properties)
                .orElse(Collections.emptyList())
                .stream()
                .map(property -> property instanceof ValuedPropertyDocument
                        ? ((ValuedPropertyDocument) property).toValuedPropertyView()
                        : ((IterableValuedPropertyDocument) property).toIterableValuedPropertyView()
                ).collect(Collectors.toList());
    }

    public static List<AbstractValuedPropertyDocument> fromAbstractDomainInstances(final List<AbstractValuedProperty> abstractValuedProperties) {
        return Optional.ofNullable(abstractValuedProperties)
                .orElse(Collections.emptyList())
                .stream()
                .map(abstractValuedProperty -> abstractValuedProperty instanceof ValuedProperty
                        ? new ValuedPropertyDocument((ValuedProperty) abstractValuedProperty)
                        : new IterableValuedPropertyDocument((IterableValuedProperty) abstractValuedProperty)
                ).collect(Collectors.toList());
    }

    public static List<AbstractValuedProperty> toAbstractDomainInstances(List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments) {
        return Optional.ofNullable(abstractValuedPropertyDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(document -> document instanceof ValuedPropertyDocument
                        ? ValuedPropertyDocument.toDomainInstance((ValuedPropertyDocument) document)
                        : IterableValuedPropertyDocument.toDomainInstance((IterableValuedPropertyDocument) document)
                ).collect(Collectors.toList());
    }
}
