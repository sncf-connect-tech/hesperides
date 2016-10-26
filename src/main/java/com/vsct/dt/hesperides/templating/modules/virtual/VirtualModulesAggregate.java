package com.vsct.dt.hesperides.templating.modules.virtual;

import com.google.common.eventbus.EventBus;
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

/**
 * Created by emeric_martineau on 31/05/2016.
 */
public class VirtualModulesAggregate extends AbstractModulesAggregate {

    private VirtualModuleRegistry mr;

    private VirtualTemplatePackagesAggregate tpa;

    private Models models;

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
    public void regenerateCache() {
        // Nothing
    }
}
