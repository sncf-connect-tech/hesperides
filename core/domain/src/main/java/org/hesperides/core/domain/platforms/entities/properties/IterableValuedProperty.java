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

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class IterableValuedProperty extends AbstractValuedProperty {

    List<IterablePropertyItem> items;

    public IterableValuedProperty(String name, List<IterablePropertyItem> items) {
        super(name);
        this.items = items;
    }

    @Override
    protected Stream<ValuedProperty> flattenProperties() {
        return Optional.ofNullable(items)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(IterablePropertyItem::getAbstractValuedProperties)
                .flatMap(List::stream)
                .map(AbstractValuedProperty::flattenProperties)
                .flatMap(Function.identity());
    }
}
