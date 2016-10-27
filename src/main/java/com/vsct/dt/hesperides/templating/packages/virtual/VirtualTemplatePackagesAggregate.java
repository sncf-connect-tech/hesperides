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

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.AbstractTemplatePackagesAggregate;

import java.util.Set;

/**
 * Created by emeric_martineau on 30/05/2016.
 */
public class VirtualTemplatePackagesAggregate extends AbstractTemplatePackagesAggregate {

    private final Models models;
    private VirtualTemplateRegistry templateRegistry;

    public VirtualTemplatePackagesAggregate(final EventStore store) {
        super(new EventBus(), store);

        this.templateRegistry = new VirtualTemplateRegistry();
        this.models = new Models(this.templateRegistry);
    }

    public VirtualTemplatePackagesAggregate(final EventStore store, final Set<Template> templates) {
        super(new EventBus(), store);

        this.templateRegistry = new VirtualTemplateRegistry();

        templates.stream().forEach(t -> this.templateRegistry.createOrUpdateTemplate(t));

        this.models = new Models(this.templateRegistry);
    }

    @Override
    protected TemplateRegistryInterface getTemplateRegistry() {
        return this.templateRegistry;
    }

    @Override
    protected Models getModels() {
        return this.models;
    }

    public void replay(final String stream) {
        super.replay(stream);
    }

    public void replay(final String stream, final long start, final long stop) {
        super.replay(stream, start, stop);
    }

    @Override
    public void regenerateCache() {
        // Nothing
    }

    public void clear() {
        this.templateRegistry.clear();
    }
}
