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
package org.hesperides.test.bdd.templatecontainers.builders;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class PropertyBuilder implements Serializable {

    //TODO à nettoyer

    private static final Pattern anythingBetweenMustachesPattern = Pattern.compile("\\{\\{(.*?)\\}\\}");

    private String name;
    private boolean isRequired;
    private String comment;
    private String defaultValue;
    private String pattern;
    @Getter
    private boolean isPassword;
    private List<PropertyBuilder> properties;
    private boolean isGlobal;

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
        isGlobal = false;
        return this;
    }

    public PropertyBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public PropertyBuilder withIsRequired() {
        this.isRequired = true;
        return this;
    }

    public PropertyBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public PropertyBuilder withDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public PropertyBuilder withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public PropertyBuilder withIsPassword() {
        this.isPassword = true;
        return this;
    }

    public PropertyBuilder withProperty(PropertyBuilder property) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        this.properties.add(property);
        return this;
    }

    public PropertyBuilder withIsGlobal() {
        this.isGlobal = true;
        return this;
    }

    public PropertyOutput build() {
        PropertyOutput property;
        if (properties == null) {
            property = new PropertyOutput(name, isRequired, comment, defaultValue, pattern, isPassword, null);
        } else {
            Set<PropertyOutput> childProperties = properties.stream()
                    .map(PropertyBuilder::build)
                    .collect(Collectors.toSet());
            property = new PropertyOutput(name, false, null, null, null, false, childProperties);
        }
        return property;
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
                property.append(" @comment ").append(comment);
            }
            if (!StringUtils.isEmpty(defaultValue)) {
                property.append(" @default ").append(defaultValue);
            }
            if (!StringUtils.isEmpty(pattern)) {
                property.append(" @pattern ").append(pattern);
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

    public String replacePropertiesWithValues(String input, List<ValuedPropertyIO> predefinedProperties, List<ValuedPropertyIO> properties) {
        String result = input;
        properties.addAll(predefinedProperties);
        for (String propertyName : extractProperties(input)) {
            String propertyValue = properties.stream().filter(valuedProperty -> valuedProperty.getName().equals(propertyName.trim())).map(ValuedPropertyIO::getValue).findFirst().orElse("");
            result = result.replace("{{" + propertyName + "}}", propertyValue);
        }
        return result;
    }

    /**
     * Extrait la liste des propriétés qu se trouvent entre moustaches.
     */
    public static List<String> extractProperties(String input) {
        List<String> properties = new ArrayList<>();
        Matcher matcher = anythingBetweenMustachesPattern.matcher(input);
        while (matcher.find()) {
            // TODO Pour corriger getFiles c'est ici que ça se passe (.trim()...)
            properties.add(matcher.group(1).trim());
        }
        return properties;
    }

    boolean isSimpleProperty() {
        return !hasProperties();
    }

    boolean isIterableProperty() {
        return hasProperties();
    }

    private boolean hasProperties() {
        return !CollectionUtils.isEmpty(properties);
    }
}
