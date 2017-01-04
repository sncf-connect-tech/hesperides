/*
 *
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
 *
 */

package com.vsct.dt.hesperides.templating.modules;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
* Created by william_montaz on 29/04/2015.
*/
class ModuleRegistry {

    private ConcurrentMap<ModuleKey, Module> modules = Maps.newConcurrentMap();

    void createOrUpdate(final Module module) {
        modules.put(module.getKey(), module);
    }

    boolean exists(final ModuleKey key) {
        return modules.containsKey(key);
    }

    Optional<Module> getModule(final ModuleKey key) {
        return Optional.ofNullable(modules.get(key));
    }

    void delete(final ModuleKey key) {
        modules.remove(key);
    }

    Collection<Module> getAllModules(){ return modules.values(); }
}
