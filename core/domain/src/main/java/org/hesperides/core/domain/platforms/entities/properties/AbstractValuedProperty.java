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
package org.hesperides.core.domain.platforms.entities.properties;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@NonFinal
public abstract class AbstractValuedProperty {
    String name;

    public static <T extends AbstractValuedProperty> List<T> filterAbstractValuedPropertyWithType(List<AbstractValuedProperty> properties, Class<T> clazz) {
        return Optional.ofNullable(properties)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public static List<ValuedProperty> getFlatValuedProperties(final List<AbstractValuedProperty> abstractValuedProperties) {
        return abstractValuedProperties
                .stream()
                .map(AbstractValuedProperty::flattenProperties)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    protected abstract Stream<ValuedProperty> flattenProperties();

    public static boolean containsDuplicateKeys(List<AbstractValuedProperty> list) {
        return list.stream()
                .map(AbstractValuedProperty::getName)
                .map(StringUtils::trim)
                .collect(Collectors.toSet()).size() != list.size();
    }
}
