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
