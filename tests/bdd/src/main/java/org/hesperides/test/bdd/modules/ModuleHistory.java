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

import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ModuleHistory {

    @Getter
    private List<ModuleBuilder> moduleBuilders;

    public ModuleHistory() {
        reset();
    }

    public ModuleHistory reset() {
        moduleBuilders = new ArrayList<>();
        return this;
    }

    public ModuleBuilder getFirstModuleBuilder() {
        return moduleBuilders.get(0);
    }

    public void addModuleBuilder(ModuleBuilder moduleBuilder) {
        moduleBuilders.add(SerializationUtils.clone(moduleBuilder));
    }

    public void removeModuleBuilder(ModuleBuilder moduleBuilderToRemove) {
        moduleBuilders = moduleBuilders.stream()
                .filter(existingModuleBuilder -> !existingModuleBuilder.equalsByKey(moduleBuilderToRemove))
                .collect(Collectors.toList());
    }

    public void updateModuleBuilder(ModuleBuilder moduleBuilder) {
        moduleBuilder.incrementVersionId();
        ModuleBuilder updatedModuleBuilder = SerializationUtils.clone(moduleBuilder);
        moduleBuilders = moduleBuilders.stream()
                .map(existingModuleBuilder -> existingModuleBuilder.equalsByKey(updatedModuleBuilder)
                        ? updatedModuleBuilder : existingModuleBuilder)
                .collect(Collectors.toList());
    }
}
