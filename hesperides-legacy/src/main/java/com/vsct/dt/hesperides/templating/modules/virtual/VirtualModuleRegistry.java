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

import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModuleRegistryInterface;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.templating.packages.virtual.VirtualTemplateRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by emeric_martineau on 31/05/2016.
 */
public class VirtualModuleRegistry extends VirtualTemplateRegistry implements ModuleRegistryInterface {
    private Module module;

    @Override
    public void createOrUpdateModule(final Module module) {
        this.module = module;
    }

    @Override
    public boolean existsModule(final ModuleKey key) {
        return module != null;
    }

    @Override
    public Optional<Module> getModule(final ModuleKey key) {
        return Optional.ofNullable(this.module);
    }

    @Override
    public void deleteModule(ModuleKey key) {
        this.module = null;
    }

    @Override
    public Collection<Module> getAllModules() {
        final List<Module> moduleList = new ArrayList<>();

        if (this.module != null) {
            moduleList.add(module);
        }

        return moduleList;
    }

    @Override
    public void removeFromCache(final ModuleKey key) {
        // Nothing
    }

    @Override
    public void removeAllCache() {
        // Nothing
    }

    @Override
    public void removeFromCache(final TemplatePackageKey packageKey) {
        // Nothing
    }

    public void clear() {
        super.clear();
        this.module = null;
    }
}
