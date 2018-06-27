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
import lombok.NoArgsConstructor;
import org.hesperides.domain.templatecontainers.entities.Property;
import org.hesperides.domain.templatecontainers.queries.PropertyView;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@NoArgsConstructor
public class PropertyDocument extends AbstractPropertyDocument {

    private boolean required;
    private String comment;
    private String defaultValue;
    private String pattern;
    private boolean password;

    public PropertyDocument(Property property) {
        this.name = property.getName();
        this.required = property.isRequired();
        this.comment = property.getComment();
        this.defaultValue = property.getDefaultValue();
        this.pattern = property.getPattern();
        this.password = property.isPassword();
    }

    public PropertyView toPropertyView() {
        return new PropertyView(getName(), required, comment, defaultValue, pattern, password);
    }
}
