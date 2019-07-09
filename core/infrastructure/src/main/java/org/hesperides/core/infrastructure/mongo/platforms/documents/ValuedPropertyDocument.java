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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.PropertyDocument;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class ValuedPropertyDocument extends AbstractValuedPropertyDocument {

    private String value;

    public ValuedPropertyDocument(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ValuedPropertyDocument(ValuedProperty valuedProperty) {
        name = StringUtils.trim(valuedProperty.getName());
        value = valuedProperty.getValue();
    }

    public static List<ValuedProperty> toDomainInstances(List<ValuedPropertyDocument> valuedPropertyDocuments) {
        return Optional.ofNullable(valuedPropertyDocuments)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ValuedPropertyDocument::toDomainInstance)
                .collect(Collectors.toList());
    }

    public ValuedPropertyView toView() {
        return new ValuedPropertyView(getName(), value);
    }

    public static List<ValuedPropertyDocument> fromDomainInstances(List<ValuedProperty> valuedProperties) {
        return Optional.ofNullable(valuedProperties)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ValuedPropertyDocument::new)
                .collect(Collectors.toList());
    }

    public static List<ValuedPropertyView> toValuedPropertyViews(List<ValuedPropertyDocument> valuedPropertyDocuments) {
        return Optional.ofNullable(valuedPropertyDocuments)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ValuedPropertyDocument::toView)
                .collect(Collectors.toList());
    }

    @Override
    protected ValuedProperty toDomainInstance() {
        return new ValuedProperty(name, value);
    }

    @Override
    protected List<AbstractValuedPropertyDocument> completeWithMustacheContent(List<AbstractPropertyDocument> abstractModelProperties) {
        List<PropertyDocument> matchingProperties = AbstractPropertyDocument.getFlatProperties(abstractModelProperties)
                .filter(abstractModuleProperty -> name.equals(abstractModuleProperty.getName()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(matchingProperties)) {
            // Si on ne la retrouve pas dans le module (propriété définie puis
            // valorisée puis supprimée du template) on la conserve telle quelle
            return Collections.singletonList(this);
        }
        // Il arrive qu'une propriété soit déclarée plusieurs fois avec le même nom
        // et un commentaire distinct. Dans ce cas on crée autant de propriétés valorisées
        // qu'il n'y a de propriétés déclarées
        return matchingProperties.stream()
                .map(propertyDocument -> new ValuedPropertyDocument(name, value))
                .collect(Collectors.toList());
    }
}
