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
package org.hesperides.infrastructure.mongo.templatecontainers;

import lombok.Data;
import org.hesperides.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.domain.templatecontainers.entities.IterableProperty;
import org.hesperides.domain.templatecontainers.entities.Property;
import org.hesperides.domain.templatecontainers.queries.AbstractPropertyView;
import org.hesperides.domain.templatecontainers.queries.IterablePropertyView;
import org.hesperides.domain.templatecontainers.queries.PropertyView;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class AbstractPropertyDocument {

    private String name;

    public static List<AbstractPropertyDocument> fromDomainInstances(List<AbstractProperty> abstractProperties) {
        List<AbstractPropertyDocument> abstractPropertyDocuments = new ArrayList<>();
        if (abstractProperties != null) {
            for (AbstractProperty abstractProperty : abstractProperties) {
                if (abstractProperty instanceof Property) {
                    Property property = (Property) abstractProperty;
                    PropertyDocument propertyDocument = PropertyDocument.fromDomainInstance(property);
                    abstractPropertyDocuments.add(propertyDocument);
                } else if (abstractProperty instanceof IterableProperty) {
                    IterableProperty iterableProperty = (IterableProperty) abstractProperty;
                    IterablePropertyDocument iterablePropertyDocument = IterablePropertyDocument.fromDomainInstance(iterableProperty);
                    abstractPropertyDocuments.add(iterablePropertyDocument);
                }
            }
        }
        return abstractPropertyDocuments;
    }

    public static List<AbstractPropertyView> toAbstractPropertyViews(List<AbstractPropertyDocument> abstractPropertyDocuments) {
        List<AbstractPropertyView> abstractPropertyViews = new ArrayList<>();
        if (abstractPropertyDocuments != null) {
            for (AbstractPropertyDocument abstractPropertyDocument : abstractPropertyDocuments) {
                if (abstractPropertyDocument instanceof PropertyDocument) {
                    PropertyDocument propertyDocument = (PropertyDocument) abstractPropertyDocument;
                    PropertyView propertyView = propertyDocument.toPropertyView();
                    abstractPropertyViews.add(propertyView);
                } else if (abstractPropertyDocument instanceof IterablePropertyDocument) {
                    IterablePropertyDocument iterablePropertyDocument = (IterablePropertyDocument) abstractPropertyDocument;
                    IterablePropertyView iterablePropertyView = iterablePropertyDocument.toIterableProperyView();
                    abstractPropertyViews.add(iterablePropertyView);
                }
            }
        }
        return abstractPropertyViews;
    }
}
