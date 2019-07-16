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

import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.entities.properties.diff.AbstractDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.IterableDifferingProperty;
import org.hesperides.core.domain.platforms.entities.properties.diff.PropertiesDiff;
import org.hesperides.core.domain.platforms.entities.properties.diff.SimpleDifferingProperty;
import org.hesperides.core.domain.platforms.exceptions.InvalidDiffSourceException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Value
@NonFinal
public abstract class AbstractValuedProperty {
    String name;
    //boolean notActiveForThisVersion;

    public static PropertiesDiff diff(List<AbstractValuedProperty> propertiesLeft, List<AbstractValuedProperty> propertiesRight) {

        // On construit une map pour avoir en clé le nom de la propriété et en valeur l'objet AbstractValuedProperty.
        // C'est pour simplifier le diff ensuite.
        Map<String, AbstractValuedProperty> propertiesLeftPerName = propertiesLeft.stream().collect(toMap(AbstractValuedProperty::getName, property -> property));
        Map<String, AbstractValuedProperty> propertiesRightPerName = propertiesRight.stream().collect(toMap(AbstractValuedProperty::getName, property -> property));

        // On procède au diff
        Set<AbstractValuedProperty> onlyLeft = propertiesLeft.stream().filter(property -> !propertiesRightPerName.containsKey(property.getName())).collect(Collectors.toSet());
        Set<AbstractValuedProperty> onlyRight = propertiesRight.stream().filter(property -> !propertiesLeftPerName.containsKey(property.getName())).collect(Collectors.toSet());
        Set<AbstractValuedProperty> common = propertiesLeft.stream().filter(propertiesRight::contains).collect(Collectors.toSet());
        Set<AbstractDifferingProperty> differingProperties = propertiesLeft.stream().filter(property -> propertiesRightPerName.containsKey(property.getName()))
                .filter(property -> !propertiesRightPerName.get(property.getName()).equals(property))
                .map(property -> {
                    if (property instanceof ValuedProperty) {
                        return new SimpleDifferingProperty(property.getName(), ((ValuedProperty) property).getValue(), ((ValuedProperty) propertiesRightPerName.get(property.getName())).getValue());
                    } else {
                        List<IterablePropertyItem> iterablePropertyLeftItems = ((IterableValuedProperty) property).getItems();
                        List<IterablePropertyItem> iterablePropertyRightItems = ((IterableValuedProperty) propertiesRightPerName.get(property.getName())).getItems();
                        int maxRange = Math.max(iterablePropertyLeftItems.size(), iterablePropertyRightItems.size());
                        List<PropertiesDiff> propertiesDiffList = IntStream.range(0, maxRange).mapToObj(i -> {
                            List<AbstractValuedProperty> nestedPropertiesLeft = (i >= iterablePropertyLeftItems.size()) ? Collections.emptyList() : iterablePropertyLeftItems.get(i).getAbstractValuedProperties();
                            List<AbstractValuedProperty> nestedPropertiesRight = (i >= iterablePropertyRightItems.size()) ? Collections.emptyList() : iterablePropertyRightItems.get(i).getAbstractValuedProperties();
                            return AbstractValuedProperty.diff(nestedPropertiesLeft, nestedPropertiesRight);
                        }).collect(Collectors.toList());

                        return new IterableDifferingProperty(property.getName(), propertiesDiffList);
                    }
                })
                .collect(Collectors.toSet());

        return new PropertiesDiff(onlyLeft, onlyRight, common, differingProperties);
    }

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
}