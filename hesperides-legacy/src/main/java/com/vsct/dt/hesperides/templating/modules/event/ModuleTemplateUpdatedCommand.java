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
import com.vsct.dt.hesperides.templating.modules.ModuleTemplateUpdatedEvent;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

import java.util.Optional;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleTemplateUpdatedCommand implements HesperidesCommand<ModuleTemplateUpdatedEvent> {
    private final TemplateRegistryInterface templateRegistry;
    private final ModuleWorkingCopyKey moduleKey;
    private final TemplateData templateData;

    /**
     * New version of template to need store in cache.
     */
    private Template updated;

    public ModuleTemplateUpdatedCommand(final TemplateRegistryInterface templateRegistry,
                                        final ModuleWorkingCopyKey moduleKey, final TemplateData templateData) {
        this.templateRegistry = templateRegistry;
        this.moduleKey = moduleKey;
        this.templateData = templateData;
    }


    @Override
    public void complete() {
        templateRegistry.createOrUpdateTemplate(updated);
    }

    @Override
    public ModuleTemplateUpdatedEvent apply() {
        Optional<Template> templateOptional = templateRegistry.getTemplate(
                moduleKey.getNamespace(),
                templateData.getName());

        if (templateOptional.isPresent()) {

            templateOptional.get().tryCompareVersionID(templateData.getVersionID());

            this.updated = new Template(
                    moduleKey.getNamespace(),
                    templateData.getName(),
                    templateData.getFilename(),
                    templateData.getLocation(),
                    templateData.getContent(),
                    templateData.getRights(),
                    templateData.getVersionID() + 1
            );

            return new ModuleTemplateUpdatedEvent(moduleKey.getName(), moduleKey.getVersionName(), this.updated);

        } else {
            throw new MissingResourceException("Cannot update template " + templateData + " because it does not exists");
        }
    }
}
