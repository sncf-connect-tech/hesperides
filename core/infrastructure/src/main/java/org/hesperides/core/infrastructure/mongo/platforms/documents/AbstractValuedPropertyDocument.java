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
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.IterableValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public abstract class AbstractValuedPropertyDocument {

    protected String name;

    public static List<AbstractValuedPropertyView> toAbstractValuedPropertyViews(final List<AbstractValuedPropertyDocument> properties) {
        final List<ValuedPropertyDocument> valuedPropertyDocuments = AbstractValuedPropertyDocument.getAbstractValuedPropertyDocumentWithType(properties, ValuedPropertyDocument.class);
        final List<ValuedPropertyView> valuedPropertyViews = ValuedPropertyDocument.toValuedPropertyViews(valuedPropertyDocuments);
        final List<IterableValuedPropertyDocument> iterableValuedPropertyDocuments = AbstractValuedPropertyDocument.getAbstractValuedPropertyDocumentWithType(properties, IterableValuedPropertyDocument.class);
        final List<IterableValuedPropertyView> iterableValuedPropertyViews = IterableValuedPropertyDocument.toIterableValuedPropertyViews(iterableValuedPropertyDocuments);
        List<AbstractValuedPropertyView> abstractValuedPropertyViews = new ArrayList<>(valuedPropertyViews);
        abstractValuedPropertyViews.addAll(iterableValuedPropertyViews);
        return abstractValuedPropertyViews;
    }

    private static <T extends AbstractValuedPropertyDocument> List<T> getAbstractValuedPropertyDocumentWithType(final List<AbstractValuedPropertyDocument> properties, Class<T> clazz) {
        return Optional.ofNullable(properties)
                .orElse(Collections.emptyList())
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public static List<AbstractValuedPropertyDocument> fromDomainProperties(final List<AbstractValuedProperty> properties) {
        return null;
    }
}
