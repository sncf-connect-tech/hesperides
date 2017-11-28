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

package com.vsct.dt.hesperides.templating.modules;

import java.util.Collection;
import java.util.Optional;

/**
 * Module registry interface.
 * <p>
 * Created by emeric_martineau on 15/01/2016.
 */
public interface ModuleRegistryInterface {
    void createOrUpdateModule(Module module);

    boolean existsModule(ModuleKey key);

    Optional<Module> getModule(ModuleKey key);

    void deleteModule(ModuleKey key);

    Collection<Module> getAllModules();

    /**
     * Remove item from cache.
     *
     * @param key
     */
    void removeFromCache(ModuleKey key);

    /**
     * Remove all data in cache.
     */
    void removeAllCache();
}
