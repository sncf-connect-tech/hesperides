package com.vsct.dt.hesperides.templating.modules.event;

import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.*;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

import java.util.Optional;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleDeletedCommand implements HesperidesCommand<ModuleDeletedEvent> {
    private final ModuleRegistryInterface moduleRegistry;
    private final TemplateRegistryInterface templateRegistry;
    private final ModuleKey moduleKey;

    public ModuleDeletedCommand(final ModuleRegistryInterface moduleRegistry,
                                final TemplateRegistryInterface templateRegistry,
                                final ModuleKey moduleKey) {
        this.moduleRegistry = moduleRegistry;
        this.templateRegistry = templateRegistry;
        this.moduleKey = moduleKey;
    }

    @Override
    public void complete() {
        //Delete all templates
        templateRegistry.getAllTemplates(moduleKey).forEach(template ->
                templateRegistry.deleteTemplate(moduleKey.getNamespace(), template.getName())
        );

        //Delete the module
        moduleRegistry.deleteModule(moduleKey);
    }

    @Override
    public ModuleDeletedEvent apply() {

        final Optional<Module> moduleOptional = moduleRegistry.getModule(moduleKey);

        if(moduleOptional.isPresent()) {
            return new ModuleDeletedEvent(moduleKey.getName(), moduleKey.getVersionName(), moduleKey.isWorkingCopy());

        } else {
            throw new MissingResourceException(moduleKey + " does not exist");
        }
    }
}
