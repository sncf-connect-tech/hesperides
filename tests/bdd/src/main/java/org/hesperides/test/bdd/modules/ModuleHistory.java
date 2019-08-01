/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-modulelogies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.bdd.modules;

import org.hesperides.core.presentation.io.ModuleIO;
import org.hesperides.core.presentation.io.ModuleKeyOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ModuleHistory {

    @Autowired
    private ModuleBuilder moduleBuilder;

    private List<ModuleBuilder> moduleBuilders = new ArrayList<>();
    private List<ModuleIO> modules = new ArrayList<>();

    public void reset() {
        moduleBuilders = new ArrayList<>();
        modules = new ArrayList<>();
    }

    public void addModule() {
        moduleBuilders.add(moduleBuilder);
        modules.add(moduleBuilder.build());
    }

    public List<ModuleKeyOutput> buildTechnoModules() {
        return Optional.ofNullable(modules)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(module -> new ModuleKeyOutput(module.getName(), module.getVersion(), module.getIsWorkingCopy()))
                .collect(Collectors.toList());
    }

    public List<ModuleIO> getModules() {
        return modules;
    }

    public List<ModuleBuilder> getModuleBuilders() {
        return moduleBuilders;
    }
}
