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

package com.vsct.dt.hesperides.applications.event;

import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.storage.HesperidesCommand;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 09/05/2016.
 */
public abstract class AbstractPlatformEvent<T> implements HesperidesCommand<T> {
    protected Set<ApplicationModuleData> generateSetOfModulesWithIds(Set<ApplicationModuleData> modules, Set<Integer> existingIds) {
        //Try to give an id to modules if they are missing
        existingIds.addAll(modules.stream().map(module -> module.getId()).collect(Collectors.toSet()));

        return modules.stream().map(module -> {
            //0 is the default value given to a module
            //We consider id 0 should never be given to a module
            if (module.getId() == 0) {
                int id = nextIdExcluding(existingIds);
                existingIds.add(id);

                return ApplicationModuleData
                        .withApplicationName(module.getName())
                        .withVersion(module.getVersion())
                        .withPath(module.getPath())
                        .withId(id)
                        .withInstances(module.getInstances())
                        .setWorkingcopy(module.isWorkingCopy())
                        .build();
            } else {
                return module;
            }
        }).collect(Collectors.toSet());
    }

    protected Set<ApplicationModuleData> generateSetOfModulesWithIds(Set<ApplicationModuleData> modules) {
        return generateSetOfModulesWithIds(modules, Sets.newHashSet());
    }

    private int nextIdExcluding(Set<Integer> forbiddenIds) {
        int i = 1;
        while (forbiddenIds.contains(i)) {
            i++;
        }
        return i;
    }
}
