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

import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.*;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

import java.util.Optional;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleDeletedCommand implements HesperidesCommand<ModuleDeletedEvent> {
    private final ModuleRegistryInterface moduleRegistry;
    private final TemplateRegistryInterface templateRegistry;
    private final ModuleKey moduleKey;

    public ModuleDeletedCommand(final ModuleRegistryInterface moduleRegistry,
                                final TemplateRegistryInterface templateRegistry,
                                final ModuleKey moduleKey) {
        this.moduleRegistry = moduleRegistry;
        this.templateRegistry = templateRegistry;
        this.moduleKey = moduleKey;
    }

    @Override
    public void complete() {
        //Delete all templates
        templateRegistry.getAllTemplates(moduleKey).forEach(template ->
                templateRegistry.deleteTemplate(moduleKey.getNamespace(), template.getName())
        );

        //Delete the module
        moduleRegistry.deleteModule(moduleKey);
    }

    @Override
    public ModuleDeletedEvent apply() {

        final Optional<Module> moduleOptional = moduleRegistry.getModule(moduleKey);

        if(moduleOptional.isPresent()) {
            return new ModuleDeletedEvent(moduleKey.getName(), moduleKey.getVersionName(), moduleKey.isWorkingCopy());

        } else {
            throw new MissingResourceException(moduleKey + " does not exist");
        }
    }
}
