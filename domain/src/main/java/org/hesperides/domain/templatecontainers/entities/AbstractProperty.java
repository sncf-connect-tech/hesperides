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
package org.hesperides.domain.templatecontainers.entities;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.codes.IterableCode;
import com.github.mustachejava.codes.ValueCode;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Value
@NonFinal
public abstract class AbstractProperty {

    String name;

    public static List<AbstractProperty> extractPropertiesFromTemplates(Collection<Template> templates) {
        List<AbstractProperty> properties = new ArrayList<>();
        if (templates != null) {
            templates.forEach((template) -> properties.addAll(extractPropertiesFromTemplate(template)));
        }
        return properties;
    }

    public static List<AbstractProperty> extractPropertiesFromTemplate(Template template) {
        List<AbstractProperty> properties = new ArrayList<>();
        if (template != null) {
            properties.addAll(extractPropertiesFromStringContent(template.getFilename()));
            properties.addAll(extractPropertiesFromStringContent(template.getLocation()));
            properties.addAll(extractPropertiesFromStringContent(template.getContent()));
        }
        return properties;
    }

    public static List<AbstractProperty> extractPropertiesFromStringContent(String content) {
        List<AbstractProperty> properties = new ArrayList<>();
        Mustache mustache = getMustacheInstanceFromStringContent(content);
        for (Code code : mustache.getCodes()) {
            if (code instanceof ValueCode) {
                String propertyDefinition = code.getName();
                Property property = Property.extractPropertyFromStringDefinition(propertyDefinition);
                if (property != null) {
                    properties.add(property);
                }
            } else if (code instanceof IterableCode) {
                IterableProperty iterableProperty = IterableProperty.extractIterablePropertyFromMustacheCode((IterableCode) code);
                if (iterableProperty != null) {
                    properties.add(iterableProperty);
                }
            }
        }
        return properties;
    }

    public static Mustache getMustacheInstanceFromStringContent(String content) {
        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory.compile(new StringReader(content), "anything");
    }

    public static void validateProperties(List<AbstractProperty> abstractProperties) {
        if (abstractProperties != null) {
            abstractProperties.forEach(abstractProperty -> {
                if (abstractProperty instanceof Property) {
                    Property property = (Property) abstractProperty;
                    property.validate();
                }
            });
        }
    }
}
