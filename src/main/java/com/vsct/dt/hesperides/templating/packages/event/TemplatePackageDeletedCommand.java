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
import com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;

import java.util.Set;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class TemplatePackageDeletedCommand implements HesperidesCommand<TemplatePackageDeletedEvent> {
    private final TemplateRegistryInterface templateRegistry;
    private final TemplatePackageKey packageKey;

    /**
     * Template to delete.
     */
    private Set<Template> templates;

    public TemplatePackageDeletedCommand(final TemplateRegistryInterface templateRegistry,
                                         final TemplatePackageKey packageKey) {
        this.templateRegistry = templateRegistry;
        this.packageKey = packageKey;
    }

    @Override
    public TemplatePackageDeletedEvent apply() {
        templates = templateRegistry.getAllTemplates(packageKey);

        if(templates.size() == 0){
            throw new MissingResourceException("There is no template package " + packageKey);
        } else {
            return new TemplatePackageDeletedEvent(packageKey.getName(), packageKey.getVersionName(),
                    packageKey.isWorkingCopy());
        }
    }

    @Override
    public void complete() {
        templates.forEach(template ->
                templateRegistry.deleteTemplate(packageKey.getNamespace(), template.getName())
        );
    }
}
