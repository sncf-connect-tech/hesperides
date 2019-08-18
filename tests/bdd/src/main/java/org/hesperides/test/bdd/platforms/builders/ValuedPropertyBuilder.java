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
package org.hesperides.test.bdd.platforms.builders;

import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValuedPropertyBuilder implements Serializable {

    private String name;
    private String value;
    //itérables ?

    public ValuedPropertyBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ValuedPropertyBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public static Set<ValuedPropertyIO> build(List<ValuedPropertyBuilder> valuedPropertyBuilders) {
        return valuedPropertyBuilders.stream().map(ValuedPropertyBuilder::build).collect(Collectors.toSet());
    }

    public ValuedPropertyIO build() {
        return new ValuedPropertyIO(name, value);
    }
}
