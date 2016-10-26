package com.vsct.dt.hesperides.templating.modules.event;

import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModuleRegistryInterface;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyUpdatedEvent;

import java.util.Optional;

/**
 * Created by emeric_martineau on 06/05/2016.
 */
public class ModuleWorkingCopyUpdatedCommand implements HesperidesCommand<ModuleWorkingCopyUpdatedEvent> {
    private final ModuleRegistryInterface moduleRegistry;
    private final ModuleKey wcInfo;
    private final Module module;

    /**
     * New version of module to need store in cache.
     */
    private Module updatedModule;

    public ModuleWorkingCopyUpdatedCommand(final ModuleRegistryInterface moduleRegistry,
                                           final ModuleKey wcInfo, final Module module) {
        this.moduleRegistry = moduleRegistry;
        this.wcInfo = wcInfo;
        this.module = module;
    }

    @Override
    public void complete() {
        moduleRegistry.createOrUpdateModule(updatedModule);
    }

    @Override
    public ModuleWorkingCopyUpdatedEvent apply() {
        Optional<Module> moduleOptional = moduleRegistry.getModule(wcInfo);

        if(moduleOptional.isPresent()) {

            moduleOptional.get().tryCompareVersionID(module.getVersionID());
            this.updatedModule = new Module(wcInfo, module.getTechnos(), module.getVersionID() + 1);

            return new ModuleWorkingCopyUpdatedEvent(this.updatedModule);

        } else {
            throw new MissingResourceException(
                    String.format(
                            "Cannot update because module working copy %s does not exists", wcInfo));
        }
    }
}
