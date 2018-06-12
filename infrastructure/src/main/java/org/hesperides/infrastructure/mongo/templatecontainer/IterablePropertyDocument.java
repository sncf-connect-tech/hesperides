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
package org.hesperides.infrastructure.mongo.templatecontainer;

import lombok.Data;
import org.hesperides.domain.templatecontainer.entities.IterableProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class IterablePropertyDocument {

    private String name;
    private PropertyDocument property;

    public static List<IterablePropertyDocument> fromDomainInstances(List<IterableProperty> iterableProperties) {
        List<IterablePropertyDocument> iterablePropertyDocuments = null;
        if (iterableProperties != null) {
            iterablePropertyDocuments = iterableProperties.stream().map(IterablePropertyDocument::fromDomainInstance).collect(Collectors.toList());
        }
        return iterablePropertyDocuments;
    }

    public static IterablePropertyDocument fromDomainInstance(IterableProperty iterableProperty) {
        IterablePropertyDocument iterablePropertyDocument = null;
        if (iterableProperty != null) {
            iterablePropertyDocument = new IterablePropertyDocument();
            iterablePropertyDocument.setName(iterableProperty.getName());
            iterablePropertyDocument.setProperty(PropertyDocument.fromDomainInstance(iterableProperty.getProperty()));
        }
        return iterablePropertyDocument;
    }
}
