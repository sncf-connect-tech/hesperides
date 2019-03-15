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
package org.hesperides.core.presentation.io.platforms.properties;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;

import java.util.*;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
public class ValuedPropertyIO extends AbstractValuedPropertyIO {

    String value;

    public ValuedPropertyIO(String name, String value) {
        super(name);
        this.value = value;
    }

    public ValuedPropertyIO(ValuedPropertyView valuedPropertyView) {
        super(valuedPropertyView.getName());
        this.value = valuedPropertyView.getValue();
    }

    public ValuedProperty toDomainInstance() {
        return new ValuedProperty(getName(), value);
    }

    public static List<ValuedProperty> toDomainInstances(Set<ValuedPropertyIO> valuedPropertyOutputs) {
        return Optional.ofNullable(valuedPropertyOutputs)
                .orElse(Collections.emptySet())
                .stream()
                .map(ValuedPropertyIO::toDomainInstance)
                .collect(Collectors.toList());
    }

    public static Set<ValuedPropertyIO> fromValuedPropertyViews(List<ValuedPropertyView> valuedPropertyViews) {
        return Optional.ofNullable(valuedPropertyViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(ValuedPropertyIO::new)
                .collect(Collectors.toSet());
    }

    // `equals` et `hashCode` sont surchargés afin de reproduire le comportement du legacy qui est :
    // une propriété valorisée doit être unique par son nom, lorsqu'on utilise un `Set`,
    // c'est-à-dire lorsqu'on veut récupérer la liste des propriétés valorisées d'un module déployé
    // => org.hesperides.core.presentation.io.platforms.properties.PropertiesIO

    @Override
    public int hashCode() {
        return Objects.hash(this.getName());
    }

    @Override
    public boolean equals(Object object) {
        boolean equals;
        if (this == object) {
            equals = true;
        } else if (object == null || getClass() != object.getClass()) {
            equals = false;
        } else {
            final ValuedPropertyIO otherProperty = (ValuedPropertyIO) object;
            equals = Objects.equals(this.getName(), otherProperty.getName());
        }
        return equals;
    }
}
