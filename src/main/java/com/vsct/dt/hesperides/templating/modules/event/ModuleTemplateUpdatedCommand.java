package com.vsct.dt.hesperides.templating.modules.event;

import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.*;
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

        if(templateOptional.isPresent()){

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
