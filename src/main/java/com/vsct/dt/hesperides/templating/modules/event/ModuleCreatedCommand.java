package com.vsct.dt.hesperides.templating.modules.event;

import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.*;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRegistryInterface;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleCreatedCommand implements HesperidesCommand<ModuleCreatedEvent> {
    private final ModuleRegistryInterface moduleRegistry;
    private final TemplateRegistryInterface templateRegistry;
    private final ModuleKey newModuleKey;
    private final Module moduleSource;
    private final Set<Template> templatesFrom;

    /**
     * New version of module to need store in cache.
     */
    private Module module;

    /**
     * New version of template to need store in cache.
     */
    private Set<Template> newTemplates;

    public ModuleCreatedCommand(final ModuleRegistryInterface moduleRegistry,
                                final TemplateRegistryInterface templateRegistry, final ModuleKey newModuleKey,
                                final Module moduleSource, final Set<Template> templatesFrom) {
        this.moduleRegistry = moduleRegistry;
        this.templateRegistry = templateRegistry;
        this.newModuleKey = newModuleKey;
        this.moduleSource = moduleSource;
        this.templatesFrom = templatesFrom;
    }

    @Override
    public void complete() {
        moduleRegistry.createOrUpdateModule(module);

        newTemplates.stream().forEach(template -> templateRegistry.createOrUpdateTemplate(template));
    }

    @Override
    public ModuleCreatedEvent apply() {
        if (moduleRegistry.existsModule(newModuleKey)) {
            throw new DuplicateResourceException("Module " + newModuleKey + " already exists");
        }

        module = new Module(newModuleKey, moduleSource.getTechnos(), 1L);

        newTemplates = templatesFrom.stream().map(template -> {
            Template newTemplate = new Template(
                    newModuleKey.getNamespace(),
                    template.getName(),
                    template.getFilename(),
                    template.getLocation(),
                    template.getContent(),
                    template.getRights(),
                    1L
            );

            return newTemplate;
        }).collect(Collectors.toSet());

        return new ModuleCreatedEvent(module, newTemplates);
    }
}
