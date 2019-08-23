/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
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
package org.hesperides.test.bdd.platforms;

import org.apache.commons.lang3.SerializationUtils;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlatformHistory {

    private List<PlatformBuilder> platformBuilders;

    public PlatformHistory() {
        reset();
    }

    public PlatformHistory reset() {
        platformBuilders = new ArrayList<>();
        return this;
    }

    public void addPlatformBuilder(PlatformBuilder platformBuilder) {
        platformBuilders.add(SerializationUtils.clone(platformBuilder));
    }

    public void removePlatformBuilder(PlatformBuilder platformBuilderToRemove) {
        platformBuilders = platformBuilders.stream()
                .filter(existingPlatformBuilder -> !existingPlatformBuilder.equalsByKey(platformBuilderToRemove))
                .collect(Collectors.toList());
    }

    public void updatePlatformBuilder(PlatformBuilder platformBuilder) {
        platformBuilder.incrementVersionId();
        platformBuilder.setDeployedModuleIds();
        PlatformBuilder updatedPlatformBuilder = SerializationUtils.clone(platformBuilder);
        platformBuilders = platformBuilders.stream()
                .map(existingPlatformBuilder -> existingPlatformBuilder.equalsByKey(updatedPlatformBuilder)
                        ? updatedPlatformBuilder : existingPlatformBuilder)
                .collect(Collectors.toList());
    }

    public List<ModulePlatformsOutput> buildModulePlatforms(DeployedModuleBuilder moduleToLookFor) {
        return platformBuilders.stream()
                .filter(platformBuilder -> platformBuilder.getDeployedModuleBuilders()
                        .stream()
                        .anyMatch(deployedModuleBuilder -> deployedModuleBuilder.equalsByKey(moduleToLookFor)))
                .map(platformBuilder -> new ModulePlatformsOutput(platformBuilder.getApplicationName(), platformBuilder.getPlatformName()))
                .collect(Collectors.toList());
    }
}
