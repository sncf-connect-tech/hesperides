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

import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class ModelBuilder {

    private List<PropertyOutput> properties;
    private List<PropertyOutput> iterableProperties;

    public ModelBuilder() {
        reset();
    }

    public ModelBuilder reset() {
        properties = new ArrayList<>();
        iterableProperties = new ArrayList<>();
        return this;
    }

    public ModelBuilder withProperty(PropertyOutput property) {
        properties.add(property);
        return this;
    }

    public ModelBuilder withIterableProperty(PropertyOutput property) {
        iterableProperties.add(property);
        return this;
    }

    public ModelOutput build() {
        return new ModelOutput(new HashSet<>(properties), new HashSet<>(iterableProperties));
    }

    public void removeProperties(List<PropertyOutput> properties) {
        this.properties.removeAll(properties);
    }

    public List<PropertyOutput> getProperties() {
        return properties;
    }

    public boolean containsProperty(String name) {
        return properties.stream().anyMatch(property -> name.equals(property.getName()));
    }
}
