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

import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.TemplateDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;

import java.util.Optional;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class TemplateDeletedCommand implements HesperidesCommand<TemplateDeletedEvent> {
    private final TemplateRegistryInterface templateRegistry;
    private final TemplatePackageKey packageKey;
    private final String templateName;

    /**
     * Template to delete.
     */
    private Template template;

    public TemplateDeletedCommand(final TemplateRegistryInterface templateRegistry, final TemplatePackageKey packageKey,
                                  final String templateName) {
        this.templateRegistry = templateRegistry;
        this.packageKey = packageKey;
        this.templateName = templateName;
    }

    @Override
    public TemplateDeletedEvent apply() {
        Optional<Template> templateOptional = templateRegistry.getTemplate(packageKey.getNamespace(), templateName);

        if(templateOptional.isPresent()){
            template = templateOptional.get();

            return new TemplateDeletedEvent(template.getNamespace(), template.getName(), template.getVersionID());

        } else {
            throw new MissingResourceException("Impossible to delete template " + templateName + " because it does not exist");
        }
    }

    @Override
    public void complete() {
        templateRegistry.deleteTemplate(template.getNamespace(), template.getName());
    }
}
