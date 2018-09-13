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
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.InstanceModelView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public ValuedPropertyDocument(ValuedProperty valuedProperty) {
        this.name = valuedProperty.getName();
        this.value = valuedProperty.getValue();
    }

    public ValuedPropertyView toValuedPropertyView() {
        return new ValuedPropertyView(getName(), value);
    }

    public InstanceModelView.InstanceModelPropertyView toInstanceModelPropertyView() {
        return new InstanceModelView.InstanceModelPropertyView(getName(), "",false, null, null, false);
    }

    public static List<ValuedPropertyDocument> fromDomainInstances(List<ValuedProperty> valuedProperties) {
        return Optional.ofNullable(valuedProperties)
                .orElse(Collections.emptyList())
                .stream()
                .map(ValuedPropertyDocument::new)
                .collect(Collectors.toList());
    }

    public static List<ValuedPropertyView> toValuedPropertyViews(List<ValuedPropertyDocument> valuedPropertyDocuments) {
        return Optional.ofNullable(valuedPropertyDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(ValuedPropertyDocument::toValuedPropertyView)
                .collect(Collectors.toList());
    }

    public static List<InstanceModelView.InstanceModelPropertyView> toInstanceModelPropertyViews(List<ValuedPropertyDocument> valuedPropertyDocuments) {
        return Optional.ofNullable(valuedPropertyDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(ValuedPropertyDocument::toInstanceModelPropertyView)
                .collect(Collectors.toList());
    }
}
