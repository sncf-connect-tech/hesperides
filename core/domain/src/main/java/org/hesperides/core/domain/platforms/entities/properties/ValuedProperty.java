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
package org.hesperides.core.domain.platforms.entities.properties;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class ValuedProperty extends AbstractValuedProperty {

    String mustacheContent;
    String value;
    String defaultValue;
    boolean isPassword;

    public ValuedProperty(String mustacheContent, String name, String value, String defaultValue, boolean isPassword) {
        super(name);
        this.mustacheContent = mustacheContent;
        this.value = value;
        this.defaultValue = defaultValue;
        this.isPassword = isPassword;
    }

    public List<String> extractValuesBetweenCurlyBrackets() {
        return Optional.ofNullable(StringUtils.substringsBetween(value, "{{", "}}"))
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static boolean isInstanceProperty(String propertyName, List<ValuedProperty> globalProperties, List<ValuedProperty> moduleProperties) {
        return propertyIsNotInProperties(propertyName, globalProperties) && propertyIsNotInProperties(propertyName, moduleProperties);
    }

    private static boolean propertyIsNotInProperties(String propertyName, List<ValuedProperty> properties) {
        return Optional.ofNullable(properties)
                .orElse(Collections.emptyList())
                .stream()
                .noneMatch(property -> property.getName().equals(propertyName));
    }
}
