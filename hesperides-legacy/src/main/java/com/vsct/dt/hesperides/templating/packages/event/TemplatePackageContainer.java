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

package com.vsct.dt.hesperides.templating.packages.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.event.TemplateContainerInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplatePackageContainer implements TemplateContainerInterface {
    /**
     * Current module read.
     */
    @JsonProperty("template")
    private final Map<String, Template> template = new HashMap<>();

    @JsonCreator
    private TemplatePackageContainer(@JsonProperty("template") final Map<String, Template> template) {
        if (template != null) {
            this.template.putAll(template);
        }
    }

    public TemplatePackageContainer() {
        // Nothing !
    }

    /**
     * Getter of current module.
     *
     * @return module
     */
    @Override
    public Template getTemplate(final String name) {
        return this.template.get(name);
    }

    /**
     * Add template into cache.
     *
     * @param template template
     */
    @Override
    public void addTemplate(final Template template) {
        this.template.put(template.getName(), template);
    }

    /**
     * Remove template in cache.
     *
     * @param name name of template
     */
    @Override
    public void removeTemplate(final String name) {
        this.template.remove(name);
    }

    /**
     * Return list of template.
     *
     * @return set of template
     */
    @Override
    public Set<Template> loadAllTemplate() {
        return this.template.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    @Override
    public void setModule(Module m) {
        // Nothing
    }

    @Override
    public Module getModule() {
        return null;
    }

    /**
     * Clear platform for reuse object.
     */
    @Override
    public void clear() {
        this.template.clear();
    }
}
