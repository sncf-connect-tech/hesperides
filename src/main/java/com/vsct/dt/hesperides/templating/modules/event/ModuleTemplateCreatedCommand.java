package com.vsct.dt.hesperides.templating.modules.event;

import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleTemplateCreatedCommand implements HesperidesCommand<ModuleTemplateCreatedEvent> {
    private final TemplateRegistryInterface templateRegistry;
    private final ModuleKey wcInfo;
    private final TemplateData templateData;

    /**
     * New version of template to need store in cache.
     */
    private Template created;

    public ModuleTemplateCreatedCommand(final TemplateRegistryInterface templateRegistry, final ModuleKey wcInfo,
                                        final TemplateData templateData) {
        this.templateRegistry = templateRegistry;
        this.wcInfo = wcInfo;
        this.templateData = templateData;
    }


    @Override
    public void complete() {
        templateRegistry.createOrUpdateTemplate(this.created);
    }

    @Override
    public ModuleTemplateCreatedEvent apply() {

        if (templateRegistry.existsTemplate(wcInfo.getNamespace(), templateData.getName()))
            throw new DuplicateResourceException("Cannot create template " + templateData + " because it already exists");

        this.created = new Template(
                wcInfo.getNamespace(),
                templateData.getName(),
                templateData.getFilename(),
                templateData.getLocation(),
                templateData.getContent(),
                templateData.getRights(),
                1L
        );

        return new ModuleTemplateCreatedEvent(wcInfo.getName(), wcInfo.getVersion().getVersionName(), this.created);
    }
}
