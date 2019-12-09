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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class ValuedProperty extends AbstractValuedProperty {

    String value;

    public ValuedProperty(String name, String value) {
        super(name);
        this.value = value;
    }

    public List<String> extractInstanceProperties(List<ValuedProperty> globalProperties, List<ValuedProperty> moduleProperties) {
        return extractValuesBetweenCurlyBrackets(value)
                .stream()
                .filter(valueBetweenCurlyBrackets -> isInstanceProperty(getName(), valueBetweenCurlyBrackets, globalProperties, moduleProperties))
                .collect(Collectors.toList());
    }

    public static List<String> extractValuesBetweenCurlyBrackets(String value) {
        return streamValuesBetweenCurlyBrackets(value)
                .collect(Collectors.toList());
    }

    public static Stream<String> streamValuesBetweenCurlyBrackets(String value) {
        return Optional.ofNullable(StringUtils.substringsBetween(value, "{{", "}}"))
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .map(String::trim);
    }

    /**
     * Une propriété déclarée dans une valorisation de propriété de module est considérée
     * comme propriété d'instance si elle n'est pas dans la liste des propriétés globales
     * de la plateforme ni dans celle des propriétés du module, sauf si c'est elle-même.
     */
    private static boolean isInstanceProperty(String currentPropertyName,
                                              String valueBetweenCurlyBrackets,
                                              List<ValuedProperty> globalProperties,
                                              List<ValuedProperty> moduleProperties) {
        return (propertyIsNotInProperties(valueBetweenCurlyBrackets, globalProperties) &&
                propertyIsNotInProperties(valueBetweenCurlyBrackets, moduleProperties))
                || currentPropertyName.equals(valueBetweenCurlyBrackets);
    }

    private static boolean propertyIsNotInProperties(String propertyName, List<ValuedProperty> properties) {
        // Lucas 2019/02/20: d'après mes tests avec JProfiler, cette méthode est un hotspot
        // lors de l'ajout de la migration de plateforme.
        // Pour optimiser je me passe donc d'Optional et de lambda ici:
        if (properties == null || propertyName == null) {
            return false;
        }
        return properties.stream().map(ValuedProperty::getName).noneMatch(propertyName::equals);
    }

    @Override
    protected Stream<ValuedProperty> flattenProperties() {
        return Stream.of(this);
    }
}
