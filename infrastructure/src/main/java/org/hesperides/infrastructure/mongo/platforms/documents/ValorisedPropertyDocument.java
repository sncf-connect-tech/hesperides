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
import org.hesperides.domain.platforms.entities.properties.ValorisedProperty;
import org.hesperides.domain.platforms.queries.views.properties.ValorisedPropertyView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class ValorisedPropertyDocument extends AbstractValorisedPropertyDocument {

    private String value;

    public static List<ValorisedPropertyDocument> fromDomainInstances(List<ValorisedProperty> valorisedProperties) {
        List<ValorisedPropertyDocument> valorisedPropertyDocuments = null;
        if (valorisedProperties != null) {
            valorisedPropertyDocuments = valorisedProperties.stream().map(ValorisedPropertyDocument::fromDomainInstance).collect(Collectors.toList());
        }
        return valorisedPropertyDocuments;
    }

    public static ValorisedPropertyDocument fromDomainInstance(ValorisedProperty valorisedProperty) {
        ValorisedPropertyDocument valorisedPropertyDocument = new ValorisedPropertyDocument();
        valorisedPropertyDocument.setName(valorisedProperty.getName());
        valorisedPropertyDocument.setValue(valorisedProperty.getValue());
        return valorisedPropertyDocument;
    }

    public static List<ValorisedPropertyView> toValorisedPropertyViews(List<ValorisedPropertyDocument> valorisedPropertyDocuments) {
        List<ValorisedPropertyView> valorisedPropertyViews = null;
        if (valorisedPropertyDocuments != null) {
            valorisedPropertyViews = valorisedPropertyDocuments.stream().map(ValorisedPropertyDocument::toValorisedPropertyView).collect(Collectors.toList());
        }
        return valorisedPropertyViews;
    }

    public ValorisedPropertyView toValorisedPropertyView() {
        return new ValorisedPropertyView(getName(), value);
    }
}
