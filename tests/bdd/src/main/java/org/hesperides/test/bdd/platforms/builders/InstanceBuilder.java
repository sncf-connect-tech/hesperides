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

import org.hesperides.core.presentation.io.platforms.InstanceIO;
import org.hesperides.core.presentation.io.platforms.properties.ValuedPropertyIO;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InstanceBuilder implements Serializable {

    private String name;
    private Set<ValuedPropertyIO> valuedProperties;

    public InstanceBuilder() {
        reset();
    }

    public InstanceBuilder reset() {
        name = "instance-name";
        valuedProperties = new HashSet<>();
        return this;
    }

    public static List<InstanceIO> build(List<InstanceBuilder> instanceBuilders) {
        return instanceBuilders
                .stream()
                .map(InstanceBuilder::build)
                .collect(Collectors.toList());
    }

    public InstanceIO build() {
        return new InstanceIO(name, valuedProperties);
    }

    public void withValuedProperty(String name, String value) {
        valuedProperties.add(new ValuedPropertyIO(name, value));
    }
}