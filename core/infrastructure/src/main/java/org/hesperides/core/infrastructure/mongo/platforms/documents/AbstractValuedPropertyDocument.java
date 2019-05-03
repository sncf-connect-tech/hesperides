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
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.IterablePropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.PropertyDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
public abstract class AbstractValuedPropertyDocument {

    protected String name;

    public static List<AbstractValuedPropertyDocument> fromAbstractDomainInstances(final List<AbstractValuedProperty> abstractValuedProperties) {
        return Optional.ofNullable(abstractValuedProperties)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(abstractValuedProperty -> abstractValuedProperty instanceof ValuedProperty
                        ? new ValuedPropertyDocument((ValuedProperty) abstractValuedProperty)
                        : new IterableValuedPropertyDocument((IterableValuedProperty) abstractValuedProperty)
                ).collect(Collectors.toList());
    }

    static List<AbstractValuedProperty> toAbstractDomainInstances(List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments) {
        return Optional.ofNullable(abstractValuedPropertyDocuments)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(AbstractValuedPropertyDocument::toDomainInstance)
                .collect(Collectors.toList());
    }

    public static List<AbstractValuedPropertyView> toViews(final List<AbstractValuedPropertyDocument> properties) {
        return Optional.ofNullable(properties)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(AbstractValuedPropertyDocument::toView)
                .collect(Collectors.toList());
    }

    /**
     * Complète les propriétés avec la valeur définie entre moustaches dans le template.
     * Cela permet d'utiliser le framework Mustache lors de la valorisation des templates.
     *
     * @see org.hesperides.core.application.files.FileUseCases#valorizeWithModuleAndGlobalAndInstanceProperties
     */
    public static List<AbstractValuedPropertyDocument> completePropertiesWithMustacheContent(List<AbstractValuedPropertyDocument> abstractValuedProperties,
                                                                                             List<AbstractPropertyDocument> abstractModelProperties) {
        // `Collectors.toSet()` est important car il permet d'éviter les doublons
        // créés par la méthode `completeWithMustacheContent`
        return new ArrayList<>(abstractValuedProperties
                .stream()
                .map(abstractValuedProperty -> abstractValuedProperty.completeWithMustacheContent(abstractModelProperties))
                .flatMap(List::stream)
                .collect(Collectors.toSet()));
    }

    private static boolean propertyHasValue(String propertyName, List<AbstractValuedPropertyDocument> abstractValuedProperties) {
        return abstractValuedProperties.stream()
                .filter(ValuedPropertyDocument.class::isInstance)
                .map(ValuedPropertyDocument.class::cast)
                .anyMatch(valuedProperty -> valuedProperty.getName().equals(propertyName) &&
                        StringUtils.isNotEmpty(valuedProperty.getValue()));
    }

    protected abstract AbstractValuedProperty toDomainInstance();

    protected abstract AbstractValuedPropertyView toView();

    protected abstract List<AbstractValuedPropertyDocument> completeWithMustacheContent(List<AbstractPropertyDocument> abstractModelProperties);
}
