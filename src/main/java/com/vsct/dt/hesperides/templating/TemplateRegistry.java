/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.templating;

import com.google.common.collect.Maps;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class TemplateRegistry {

    private static class Key {
        private String namespace;
        private String name;

        private Key(final String namespace, final String name) {
            this.namespace = namespace;
            this.name = name;
        }

        public Key(final Template template) {
            this(template.getNamespace(), template.getName());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;

            Key key = (Key) o;

            if (name != null ? !name.equals(key.name) : key.name != null) return false;
            if (namespace != null ? !namespace.equals(key.namespace) : key.namespace != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = namespace != null ? namespace.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

    }

    private ConcurrentMap<Key, Template> templates = Maps.newConcurrentMap();

    public Collection<Template> all() {
        return templates.values();
    }

    public Optional<Template> getTemplate(final String namespace, final String name) {
        return Optional.ofNullable(templates.get(new Key(namespace, name)));
    }

    public Optional<Template> getTemplate(final TemplatePackageKey packageKey, final String name) {
        return getTemplate(packageKey.getNamespace(), name);
    }

    public boolean exists(final String namespace, final String name) {
        return templates.containsKey(new Key(namespace, name));
    }

    public void createOrUpdate(final Template template) {
        templates.put(new Key(template), template);
    }

    public void delete(final String namespace, final String name) {
        templates.remove(new Key(namespace, name));
    }

    public Set<Template> getAllForNamespace(final String namespace) {
        return templates.keySet().stream()
                .filter(key -> key.namespace.equals(namespace))
                .map(key -> templates.get(key))
                .collect(Collectors.toSet());
    }

    public Set<Template> getAll(TemplatePackageKey packageKey) {
        return getAllForNamespace(packageKey.getNamespace());
    }

    public boolean hasNamespace(final String namespace) {
        return templates.keySet().stream().anyMatch(key -> key.namespace.equals(namespace));
    }
}