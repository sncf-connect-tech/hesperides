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

package com.vsct.dt.hesperides.templating.modules.virtual;

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.applications.virtual.VirtualExecutorService;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.templating.models.Models;
import com.vsct.dt.hesperides.templating.modules.AbstractModulesAggregate;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleRegistryInterface;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.AbstractTemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.VirtualTemplatePackagesAggregate;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by emeric_martineau on 31/05/2016.
 */
public class VirtualModulesAggregate extends AbstractModulesAggregate {

    /**
     * Virtual registry of modules.
     */
    private VirtualModuleRegistry mr;

    /**
     * Virtual registry of techno.
     */
    private VirtualTemplatePackagesAggregate tpa;

    /**
     * Models.
     */
    private Models models;

    /**
     * Convenient class that wraps the thread executor of the aggregate
     */
    private ExecutorService singleThreadPool = new VirtualExecutorService();

    public VirtualModulesAggregate(final EventStore store) {
        super(new EventBus(), store);

        this.mr = new VirtualModuleRegistry();
        this.tpa = new VirtualTemplatePackagesAggregate(store);
        this.models = new Models(this.mr);
    }

    public VirtualModulesAggregate(final EventStore store, final Module module, final Set<Template> templates) {
        this(store);

        mr.createOrUpdateModule(module);

        templates.stream().forEach(t -> mr.createOrUpdateTemplate(t));
    }

    @Override
    protected ModuleRegistryInterface getModuleRegistry() {
        return this.mr;
    }

    @Override
    protected TemplateRegistryInterface getTemplateRegistry() {
        return this.mr;
    }

    @Override
    protected AbstractTemplatePackagesAggregate getTemplatePackages() {
        return this.tpa;
    }

    @Override
    protected Models getModels() {
        return this.models;
    }

    public void clear() {
        this.mr.clear();
        this.tpa.clear();
    }

    public void replay(final String stream) {
        super.replay(stream);
    }

    public void replay(final String stream, final long start, final long stop) {
        super.replay(stream, start, stop);
    }

    @Override
    protected ExecutorService executorService() {
        return this.singleThreadPool;
    }
}
