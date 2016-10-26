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
