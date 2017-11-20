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
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

import java.util.Optional;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleTemplateDeletedCommand implements HesperidesCommand<ModuleTemplateDeletedEvent> {
    private final TemplateRegistryInterface templateRegistry;
    private final ModuleKey wcInfo;
    private final String templateName;

    /**
     * New version of template to need store in cache.
     */
    private Template templateToDelete;

    public ModuleTemplateDeletedCommand(final TemplateRegistryInterface templateRegistry, final ModuleKey wcInfo,
                                        final String templateName) {
        this.templateRegistry = templateRegistry;
        this.wcInfo = wcInfo;
        this.templateName = templateName;
    }


    @Override
    public void complete() {
        templateRegistry.deleteTemplate(templateToDelete.getNamespace(), templateToDelete.getName());
    }

    @Override
    public ModuleTemplateDeletedEvent apply() {

        Optional<Template> optionalTemplate = templateRegistry.getTemplate(wcInfo.getNamespace(), templateName);

        if(optionalTemplate.isPresent()){
            templateToDelete = optionalTemplate.get();

            return new ModuleTemplateDeletedEvent(wcInfo.getName(), wcInfo.getVersion().getVersionName(), templateName);
        } else {
            throw new MissingResourceException(
                    String.format("Impossible to delete template %s because it does not exist", templateName));
        }
    }
}
