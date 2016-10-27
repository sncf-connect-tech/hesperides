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

package com.vsct.dt.hesperides.templating.modules.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.event.TemplateContainerInterface;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Create module when recieve event.
 * Use to load a module.
 *
 * Created by emeric_martineau on 15/01/2016.
 */
public class ModuleContainer implements TemplateContainerInterface {
    /**
     * Current module read.
     */
    @JsonProperty("module")
    private Module module = null;

    /**
     * Current module read.
     */
    @JsonProperty("template")
    private final Map<String, Template> template = new ConcurrentHashMap<>();

    @JsonCreator
    private ModuleContainer(@JsonProperty("module") final Module module, @JsonProperty("template") final Map<String, Template> template) {
        this.module = module;

        if (template != null) {
            this.template.putAll(template);
        }
    }

    public ModuleContainer() {
        // nothing
    }

    /**
     * Getter of current module.
     *
     * @return module
     */
    @Override
    public Template getTemplate(final String name) {
        Template tpl;

        if (this.module == null) {
            tpl = null;
        } else {
            tpl = this.template.get(name);
        }

        return tpl;
    }

    /**
     * Add template into cache.
     *
     * @param template template
     */
    @Override
    public void addTemplate(final Template template) {
        if (this.module != null) {
            this.template.put(template.getName(), template);
        }
    }

    /**
     * Remove template in cache.
     *
     * @param name name of template
     */
    @Override
    public void removeTemplate(final String name) {
        if (this.module != null) {
            this.template.remove(name);
        }
    }

    /**
     * Return list of template.
     *
     * @return set of template
     */
    @Override
    public Set<Template> loadAllTemplate() {
        Set<Template> tpl;

        if (this.module == null) {
            tpl = ImmutableSet.of();
        } else {
            tpl = this.template.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
        }

        return tpl;
    }

    /**
     * Add module.
     *
     * @param m module
     */
    @Override
    public void setModule(final Module m) {
        this.module = m;
    }

    /**
     * Get module.
     *
     * @return module
     */
    @Override
    public Module getModule() {
        return this.module;
    }

    /**
     * Clear module for reuse object.
     */
    @Override
    public void clear() {
        this.module = null;
        this.template.clear();
    }
}
