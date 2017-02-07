/*
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
 */

package com.vsct.dt.hesperides.templating.packages.virtual;

import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 30/05/2016.
 */
public class VirtualTemplateRegistry implements TemplateRegistryInterface {
    private Map<String, Map<String, Template>> templates = new HashMap<>();

    @Override
    public Collection<Template> allTemplates() {
        final List<Template> allTemplate = new ArrayList<>();

        for (Map.Entry<String, Map<String, Template>> mapTemplate : templates.entrySet()) {
            allTemplate.addAll(
                    mapTemplate.getValue().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet()));
        }

        return allTemplate;
    }

    @Override
    public Optional<Template> getTemplate(final String namespace, final String name) {
        final Map<String, Template> currentNameSpace = this.templates.get(namespace);

        if (currentNameSpace != null && currentNameSpace.containsKey(name)) {
            return Optional.ofNullable(currentNameSpace.get(name));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Template> getTemplate(final TemplatePackageKey packageKey, final String name) {
        return getTemplate(packageKey.getNamespace(), name);
    }

    @Override
    public boolean existsTemplate(final String namespace, final String name) {
        return getTemplate(namespace, name).isPresent();
    }

    @Override
    public void createOrUpdateTemplate(final Template template) {
        Map<String, Template> currentNameSpace = this.templates.get(template.getNamespace());

        if (currentNameSpace == null) {
            currentNameSpace = new HashMap<>();
            this.templates.put(template.getNamespace(), currentNameSpace);
        }

        currentNameSpace.put(template.getName(), template);
    }

    @Override
    public void deleteTemplate(final String namespace, final String name) {
        final Map<String, Template> currentNameSpace = this.templates.get(namespace);

        if (currentNameSpace != null) {
            currentNameSpace.remove(name);

            if (currentNameSpace.size() == 0) {
                this.templates.remove(namespace);
            }
        }
    }

    @Override
    public Set<Template> getAllTemplatesForNamespace(final String namespace) {
        final Map<String, Template> currentNameSpace = this.templates.get(namespace);
        Set<Template> result;

        if (currentNameSpace == null) {
            result = new HashSet<>();
        } else {
            result = currentNameSpace.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
        }

        return result;
    }

    @Override
    public Set<Template> getAllTemplates(final TemplatePackageKey packageKey) {
        return getAllTemplatesForNamespace(packageKey.getNamespace());
    }

    @Override
    public Set<Template> getAllTemplates() {
        Set<Template> result;

        if (this.templates == null) {
            result = new HashSet<>();
        } else {
            result = this.templates.entrySet().stream().map(Map.Entry::getValue).flatMap(
                    t -> t.entrySet().stream().map(Map.Entry::getValue)
            ).collect(Collectors.toSet());
        }

        return result;
    }

    @Override
    public boolean templateHasNamespace(final String namespace) {
        return this.templates.containsKey(namespace);
    }

    @Override
    public void removeFromCache(final TemplatePackageKey packageKey) {
        // Nothing
    }

    @Override
    public void removeAllCache() {
        // Nothing
    }

    public void clear() {
        this.templates.clear();
    }
}
