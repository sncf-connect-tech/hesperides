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

        if (moduleOptional.isPresent()) {

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
