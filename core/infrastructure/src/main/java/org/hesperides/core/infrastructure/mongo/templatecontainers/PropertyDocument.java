/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
package org.hesperides.core.infrastructure.mongo.templatecontainers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.templatecontainers.entities.Property;
import org.hesperides.core.domain.templatecontainers.queries.PropertyView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.stream.Stream;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class PropertyDocument extends AbstractPropertyDocument {

    private String mustacheContent;
    private boolean isRequired;
    private String comment;
    private String defaultValue;
    private String pattern;
    private boolean isPassword;

    public PropertyDocument(Property property) {
        this.mustacheContent = property.getMustacheContent();
        this.name = property.getName();
        this.isRequired = property.isRequired();
        this.comment = property.getComment();
        this.defaultValue = property.getDefaultValue();
        this.pattern = property.getPattern();
        this.isPassword = property.isPassword();
    }

    @Override
    public Property toDomainInstance() {
        return new Property(mustacheContent, name, isRequired, comment, defaultValue, pattern, isPassword);
    }

    @Override
    public PropertyView toView() {
        return new PropertyView(getName(), mustacheContent, isRequired, comment, defaultValue, pattern, isPassword);
    }

    @Override
    protected Stream<PropertyDocument> flattenProperties() {
        return Stream.of(this);
    }
}
