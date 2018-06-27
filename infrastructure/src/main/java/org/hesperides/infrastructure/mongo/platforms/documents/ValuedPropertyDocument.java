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
package org.hesperides.infrastructure.mongo.platforms.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
@NoArgsConstructor
public class ValuedPropertyDocument extends AbstractValuedPropertyDocument {

    private String value;

    public ValuedPropertyDocument(ValuedProperty valuedProperty) {
        this.name = valuedProperty.getName();
        this.value = valuedProperty.getValue();
    }

    public ValuedPropertyView toValuedPropertyView() {
        return new ValuedPropertyView(getName(), value);
    }

    public static List<ValuedPropertyDocument> fromDomainInstances(List<ValuedProperty> valuedProperties) {
        List<ValuedPropertyDocument> valuedPropertyDocuments = null;
        if (valuedProperties != null) {
            valuedPropertyDocuments = valuedProperties.stream().map(ValuedPropertyDocument::new).collect(Collectors.toList());
        }
        return valuedPropertyDocuments;
    }

    public static List<ValuedPropertyView> toValuedPropertyViews(List<ValuedPropertyDocument> valuedPropertyDocuments) {
        List<ValuedPropertyView> valuedPropertyViews = null;
        if (valuedPropertyDocuments != null) {
            valuedPropertyViews = valuedPropertyDocuments.stream().map(ValuedPropertyDocument::toValuedPropertyView).collect(Collectors.toList());
        }
        return valuedPropertyViews;
    }
}
