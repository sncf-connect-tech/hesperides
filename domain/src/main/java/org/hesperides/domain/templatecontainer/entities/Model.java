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
package org.hesperides.domain.templatecontainer.entities;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import lombok.Value;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Value
public class Model {

    List<Property> properties;
    List<IterableProperty> iterableProperties;

    public static Model generateModelFromTemplates(Collection<Template> templates) {
        List<Property> properties = new ArrayList<>();
        List<IterableProperty> iterableProperties = new ArrayList<>();

        if (templates != null) {
            templates.forEach((template) -> {
                properties.addAll(extractPropertiesFromStringContent(template.getFilename()));
                properties.addAll(extractPropertiesFromStringContent(template.getLocation()));
                properties.addAll(extractPropertiesFromStringContent(template.getContent()));
                iterableProperties.addAll(extractIterablePropertiesFromStringContent(template.getFilename()));
                iterableProperties.addAll(extractIterablePropertiesFromStringContent(template.getLocation()));
                iterableProperties.addAll(extractIterablePropertiesFromStringContent(template.getContent()));
            });
        }

        return new Model(properties, iterableProperties);
    }

    public static List<Property> extractPropertiesFromStringContent(String content) {
        List<Property> properties = new ArrayList<>();
        Mustache mustache = getMustacheInstanceFromStringContent(content);
        for (Code code : mustache.getCodes()) {
            if (code instanceof ValueCode) {
                String propertyDefinition = code.getName();
                Property property = Property.extractPropertyFromStringDefinition(propertyDefinition);
                if (property != null) {
                    properties.add(property);
                }
            }
        }
        return properties;
    }

    public static List<IterableProperty> extractIterablePropertiesFromStringContent(String content) {
        List<IterableProperty> iterableProperties = new ArrayList<>();
        Mustache mustache = getMustacheInstanceFromStringContent(content);
        for (Code code : mustache.getCodes()) {
            if (code instanceof IterableCode) {
                iterableProperties.add(extractIterablePropertyFromMustacheCode((IterableCode) code));
            }
        }
        return iterableProperties;
    }

    /**
     * Méthode récursive permettant d'extraire les propriétés et les propriétés itérables contenues dans une propriété itérable.
     *
     * @param code
     * @return
     */
    private static IterableProperty extractIterablePropertyFromMustacheCode(IterableCode code) {
        String name = code.getName();
        List<Property> properties = new ArrayList<>();
        List<IterableProperty> iterableProperties = new ArrayList<>();

        for (Code childCode : code.getCodes()) {
            if (childCode instanceof ValueCode) {
                properties.add(Property.extractPropertyFromStringDefinition(childCode.getName()));
            } else if (childCode instanceof IterableCode) {
                iterableProperties.add(extractIterablePropertyFromMustacheCode((IterableCode) childCode));
            }
        }
        // Pour coller au legacy, on utilise les valeurs par défaut car les annotations ne sont pas ou plus utilisées dans les propriétés itératives
        return new IterableProperty(name, false, "", "", "", false, properties, iterableProperties);
    }

    private static Mustache getMustacheInstanceFromStringContent(String content) {
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory.compile(new StringReader(content), "anything");
    }
}
