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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = true)
public class ValuedProperty extends AbstractValuedProperty {

    String mustacheContent;
    String value;

    public ValuedProperty(String mustacheContent, String name, String value) {
        super(name);
        this.mustacheContent = mustacheContent;
        this.value = value;
    }

    /**
     * True si la valeur est entre moustaches et si cette valeur entre moustaches
     * ne correspond pas au nom d'une propriété globale
     */
    public boolean valueIsInstanceProperty(List<ValuedProperty> platformGlobalProperties) {
        String valueBetweenMustaches = extractValueBetweenMustaches(value);
        return StringUtils.isNotEmpty(valueBetweenMustaches) && !Optional.ofNullable(platformGlobalProperties)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(globalProperty -> globalProperty.getName().equals(valueBetweenMustaches));
    }

    private String extractValueBetweenMustaches(String value) {
        return StringUtils.substringBetween(value, "{{", "}}");
    }

    public String extractInstancePropertyNameFromValue() {
        return extractValueBetweenMustaches(value);
    }
}
