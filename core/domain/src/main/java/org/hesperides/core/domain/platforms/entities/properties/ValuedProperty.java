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
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;

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

    public static boolean instancePropertyNameIsNotInGlobalProperties(String instancePropertyName, List<ValuedProperty> globalProperties) {
        return Optional.ofNullable(globalProperties)
                .orElse(Collections.emptyList())
                .stream()
                .noneMatch(globalProperty -> globalProperty.getName().equals(instancePropertyName));
    }

    public List<String> extractInstanceProperties() {
        return Optional.ofNullable(StringUtils.substringsBetween(this.value, "{{", "}}"))
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public boolean valuedPropertyNameIsInModuleModel(List<AbstractProperty> moduleProperties) {
        return Optional.ofNullable(moduleProperties)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(moduleProperty -> moduleProperty.getName().equals(getName()));
    }
}
