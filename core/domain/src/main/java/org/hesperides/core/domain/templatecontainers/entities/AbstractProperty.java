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
package org.hesperides.core.domain.templatecontainers.entities;

import com.github.mustachejava.*;
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Value
@NonFinal
@Slf4j
public abstract class AbstractProperty {

    String name;

    public static List<AbstractProperty> extractPropertiesFromTemplates(Collection<Template> templates, String templateContainerKey) {
        Set<AbstractProperty> properties = new HashSet<>();

        Stream<AbstractProperty> propertiesFromTemplates = Optional.ofNullable(templates)
                .orElseGet(Collections::emptyList).stream()
                .map(Template::extractProperties)
                .flatMap(List::stream);
        List<AbstractProperty> mergedProperties = mergeAbstractPropertyDefinitions(propertiesFromTemplates, templateContainerKey)
                .collect(Collectors.toList());
        properties.addAll(mergedProperties);

        return new ArrayList<>(properties);
    }

    // Méthode chapeau permettant d'uniquement traiter les Property,
    // les IterableProperty n'étant pas modifiées et ressortent telles quelles
    static private Stream<AbstractProperty> mergeAbstractPropertyDefinitions(Stream<AbstractProperty> properties, String templateContainerKey) {
        // Un stream ne peut être "splitté" en 2, nous devons donc le convertir en collection:
        List<AbstractProperty> propertyList = properties.collect(Collectors.toList());
        return Stream.concat(
                propertyList.stream().filter(IterableProperty.class::isInstance),
                mergePropertyDefinitions(propertyList.stream().filter(Property.class::isInstance).map(Property.class::cast), templateContainerKey)
        );
    }

    // Déduplication des définitions de propriétés, lorsqu'elles sont employées à plusieurs endroits dans les templates.
    // La règle: lorsqu'une propriété est utilisée à plusieurs endroits,
    // il suffit qu'une de ses définitions soit annotée en @password, @default ou @required
    // pour que toutes ses occurences le soient:
    static private Stream<Property> mergePropertyDefinitions(Stream<Property> properties, String templateContainerKey) {
        Map<DedupeKey, List<Property>> propertiesPerDedupeKey = properties
                .collect(groupingBy(p -> new DedupeKey(p.getName(), p.getComment())));
        List<Property> dedupedProperties = new ArrayList<>();
        propertiesPerDedupeKey.values().forEach(propertiesWithSameNameAndComment -> {
            Property property = propertiesWithSameNameAndComment.get(0);
            String propertyName = property.getName();
            if (propertiesWithSameNameAndComment.stream().anyMatch(Property::isPassword)) {
                property = property.cloneAsPassword();
            }
            if (propertiesWithSameNameAndComment.stream().anyMatch(Property::isRequired)) {
                property = property.cloneAsRequired();
            }
            Optional<String> firstDefaultValue = propertiesWithSameNameAndComment.stream()
                    .map(Property::getDefaultValue)
                    .filter(StringUtils::isNotBlank)
                    .findFirst();
            if (firstDefaultValue.isPresent()) {
                String defaultValue = firstDefaultValue.get();
                property = property.cloneWithDefaultValue(defaultValue);
                propertiesWithSameNameAndComment.stream()
                        .map(Property::getDefaultValue)
                        .filter(StringUtils::isNotBlank)
                        .filter(otherDefaultValue -> !otherDefaultValue.equals(defaultValue))
                        .forEach(otherDefaultValue -> log.debug("{}: Property {} has several differing default values defined", templateContainerKey, propertyName));
            }
            Set<String> mustacheContents = propertiesWithSameNameAndComment.stream()
                    .map(Property::getMustacheContent)
                    .collect(Collectors.toSet());
            for (String mustacheContent : mustacheContents) {
                dedupedProperties.add(property.cloneWithMustacheContent(mustacheContent));
            }
        });
        return dedupedProperties.stream();
    }

    @Value
    private static class DedupeKey {
        private String name;
        private String comment;
    }

    public static List<AbstractProperty> extractPropertiesFromStringContent(String content) {
        List<AbstractProperty> properties = new ArrayList<>();
        Mustache mustache = getMustacheInstanceFromStringContent(content);
        for (Code code : mustache.getCodes()) {
            if (code instanceof ValueCode) {
                String propertyDefinition = code.getName();
                Property property = Property.extractProperty(propertyDefinition);
                if (property != null) {
                    properties.add(property);
                }
            } else if (code instanceof IterableCode) {
                IterableProperty iterableProperty = IterableProperty.extractIterablePropertyFromMustacheCode((IterableCode) code);
                properties.add(iterableProperty);
            }
        }
        return properties;
    }

    public static Mustache getMustacheInstanceFromStringContent(String content) {
        MustacheFactory mustacheFactory = new UnescapedValuesMustacheFactory();
        return mustacheFactory.compile(new StringReader(content), "anything");
    }

    /**
     * Cette classe permet d'éviter d'échapper les valorisations.
     * Cela a pour but d'être rétrocompatible avec le legacy et
     * c'est le comportement attendu.
     */
    private static class UnescapedValuesMustacheFactory extends DefaultMustacheFactory {
        @Override
        public void encode(String value, Writer writer) {
            try {
                writer.append(value);
            } catch (IOException e) {
                throw new MustacheException("Failed to append value: " + value);
            }
        }
    }
}