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
import org.hesperides.domain.templatecontainer.entities.Property;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class PropertyDocument {
    private String name;
    private boolean isRequired;
    private String comment;
    private String defaultValue;
    private String pattern;
    private boolean isPassword;

    public static List<PropertyDocument> fromDomainInstances(List<Property> properties) {
        List<PropertyDocument> propertyDocuments = null;
        if (properties != null) {
            propertyDocuments = properties.stream().map(PropertyDocument::fromDomainInstance).collect(Collectors.toList());
        }
        return propertyDocuments;
    }

    public static PropertyDocument fromDomainInstance(Property property) {
        PropertyDocument propertyDocument = null;
        if (property != null) {
            propertyDocument = new PropertyDocument();
            propertyDocument.setName(property.getName());
            propertyDocument.setRequired(property.isPassword());
            propertyDocument.setComment(property.getComment());
            propertyDocument.setDefaultValue(property.getDefaultValue());
            propertyDocument.setPattern(property.getPattern());
            propertyDocument.setPassword(property.isPassword());
        }
        return propertyDocument;
    }


}
