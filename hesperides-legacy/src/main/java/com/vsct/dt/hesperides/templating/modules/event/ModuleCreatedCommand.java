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

import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleCreatedEvent;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModuleRegistryInterface;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleCreatedCommand implements HesperidesCommand<ModuleCreatedEvent> {
    private final ModuleRegistryInterface moduleRegistry;
    private final TemplateRegistryInterface templateRegistry;
    private final ModuleKey newModuleKey;
    private final Module moduleSource;
    private final Set<Template> templatesFrom;

    /**
     * New version of module to need store in cache.
     */
    private Module module;

    /**
     * New version of template to need store in cache.
     */
    private Set<Template> newTemplates;

    public ModuleCreatedCommand(final ModuleRegistryInterface moduleRegistry,
                                final TemplateRegistryInterface templateRegistry, final ModuleKey newModuleKey,
                                final Module moduleSource, final Set<Template> templatesFrom) {
        this.moduleRegistry = moduleRegistry;
        this.templateRegistry = templateRegistry;
        this.newModuleKey = newModuleKey;
        this.moduleSource = moduleSource;
        this.templatesFrom = templatesFrom;
    }

    @Override
    public void complete() {
        moduleRegistry.createOrUpdateModule(module);

        newTemplates.stream().forEach(template -> templateRegistry.createOrUpdateTemplate(template));
    }

    @Override
    public ModuleCreatedEvent apply() {
        if (moduleRegistry.existsModule(newModuleKey)) {
            throw new DuplicateResourceException("Module " + newModuleKey + " already exists");
        }

        module = new Module(newModuleKey, moduleSource.getTechnos(), 1L);

        newTemplates = templatesFrom.stream().map(template -> {
            Template newTemplate = new Template(
                    newModuleKey.getNamespace(),
                    template.getName(),
                    template.getFilename(),
                    template.getLocation(),
                    template.getContent(),
                    template.getRights(),
                    1L
            );

            return newTemplate;
        }).collect(Collectors.toSet());

        return new ModuleCreatedEvent(module, newTemplates);
    }
}
