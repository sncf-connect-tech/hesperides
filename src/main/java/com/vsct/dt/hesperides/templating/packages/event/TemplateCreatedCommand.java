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

import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public class TemplateCreatedCommand implements HesperidesCommand<TemplateCreatedEvent> {
    private final TemplateRegistryInterface templateRegistry;
    private final TemplatePackageKey packageKey;
    private final TemplateData templateData;

    /**
     * Platform created.
     */
    private Template created;

    public TemplateCreatedCommand(final TemplateRegistryInterface templateRegistry, final TemplatePackageKey packageKey,
                                  final TemplateData templateData) {
        this.templateRegistry = templateRegistry;
        this.packageKey = packageKey;
        this.templateData = templateData;
    }

    @Override
    public TemplateCreatedEvent apply() {
        if (templateRegistry.existsTemplate(packageKey.getNamespace(), templateData.getName())) {
            throw new DuplicateResourceException("Cannot create template " + templateData + " because it already exists");
        }

        created = new Template(
                packageKey.getNamespace(),
                templateData.getName(),
                templateData.getFilename(),
                templateData.getLocation(),
                templateData.getContent(),
                templateData.getRights(),
                1L
        );

        return new TemplateCreatedEvent(created);
    }

    @Override
    public void complete() {
        templateRegistry.createOrUpdateTemplate(created);
    }
}
