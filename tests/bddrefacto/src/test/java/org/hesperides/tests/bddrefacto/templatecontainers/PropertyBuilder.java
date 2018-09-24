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
package org.hesperides.tests.bddrefacto.templatecontainers;

import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PropertyBuilder {


    private String name;
    private boolean isRequired;
    private String comment;
    private String defaultValue;
    private String pattern;
    private boolean isPassword;
    private List<PropertyBuilder> properties;

    public PropertyBuilder() {
        reset();
    }

    public PropertyBuilder reset() {
        name = "foo";
        isRequired = false;
        comment = "";
        defaultValue = "";
        pattern = "";
        isPassword = false;
        properties = null;
        return this;
    }

    public PropertyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public PropertyBuilder withIsRequired() {
        this.isRequired = true;
        return this;
    }

    public PropertyBuilder withComment(final String comment) {
        this.comment = comment;
        return this;
    }

    public PropertyBuilder withDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public PropertyBuilder withPattern(final String pattern) {
        this.pattern = pattern;
        return this;
    }

    public PropertyBuilder withIsPassword() {
        this.isPassword = true;
        return this;
    }

    public PropertyBuilder withProperty(final PropertyBuilder property) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        this.properties.add(property);
        return this;
    }

    public PropertyBuilder withProperties(final List<PropertyBuilder> properties) {
        this.properties = properties;
        return this;
    }

    public PropertyOutput build() {
        Set<PropertyOutput> propertyOutputs = properties == null ? null : properties
                .stream()
                .map(PropertyBuilder::build)
                .collect(Collectors.toSet());
        return new PropertyOutput(name, isRequired, comment, defaultValue, pattern, isPassword, propertyOutputs);
    }

    public String toString() {
        StringBuilder property = new StringBuilder();

        if (CollectionUtils.isEmpty(properties)) {
            property.append("{{");
            property.append(name);
            if (isRequired || !StringUtils.isEmpty(comment) || !StringUtils.isEmpty(defaultValue) || !StringUtils.isEmpty(pattern) || isPassword) {
                property.append(" |");
            }
            if (isRequired) {
                property.append(" @required");
            }
            if (!StringUtils.isEmpty(comment)) {
                property.append(" @comment " + comment);
            }
            if (!StringUtils.isEmpty(defaultValue)) {
                property.append(" @default " + defaultValue);
            }
            if (!StringUtils.isEmpty(pattern)) {
                property.append(" @pattern " + pattern);
            }
            if (isPassword) {
                property.append(" @password");
            }
            property.append("}}");

        } else {
            property.append("{{#");
            property.append(name);
            property.append("}}");
            for (PropertyBuilder propertyBuilder : properties) {
                property.append(propertyBuilder);
            }
            property.append("{{/");
            property.append(name);
            property.append("}}");
        }
        return property.toString();
    }
}
