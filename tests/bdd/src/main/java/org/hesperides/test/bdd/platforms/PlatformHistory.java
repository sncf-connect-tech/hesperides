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
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PlatformHistory {

    private Map<Platform.Key, Map<Long, Optional<PlatformBuilder>>> platformBuilders; //platformTimestampBuilders + créer une classe interne : TimestampBuilder

    public PlatformHistory() {
        reset();
    }

    public PlatformHistory reset() {
        platformBuilders = new HashMap<>();
        return this;
    }

    public void addPlatformBuilder(PlatformBuilder platformBuilder) {
        Platform.Key platformKey = platformBuilder.buildPlatformKey();
        Map<Long, Optional<PlatformBuilder>> platformTimestamps = new HashMap<>();
        PlatformBuilder currentPlatformBuilder = SerializationUtils.clone(platformBuilder);
        platformTimestamps.put(System.currentTimeMillis(), Optional.of(currentPlatformBuilder));
        platformBuilders.put(platformKey, platformTimestamps);
    }

    public void removePlatformBuilder(PlatformBuilder platformBuilderToRemove) {
        Platform.Key platformKey = platformBuilderToRemove.buildPlatformKey();
        platformBuilders.get(platformKey).put(System.currentTimeMillis(), Optional.empty());
    }

    public void updatePlatformBuilder(PlatformBuilder platformBuilder) {
        platformBuilder.incrementVersionId();
        platformBuilder.setDeployedModuleIds();
        PlatformBuilder updatedPlatformBuilder = SerializationUtils.clone(platformBuilder);

        Platform.Key platformKey = platformBuilder.buildPlatformKey();
        Map<Long, Optional<PlatformBuilder>> platformTimestamps = platformBuilders.get(platformKey);
        platformTimestamps.put(System.currentTimeMillis(), Optional.of(updatedPlatformBuilder));
        platformBuilders.put(platformKey, platformTimestamps);
    }

    private Optional<PlatformBuilder> getLastOf(Map<Long, Optional<PlatformBuilder>> timestampPlatformMap) { //timestampBuilders/timestampBuilderMap
        return timestampPlatformMap
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByKey())
                .flatMap(Map.Entry::getValue);
    }

    public List<ModulePlatformsOutput> buildModulePlatforms(DeployedModuleBuilder moduleToLookFor) {
        return platformBuilders.values().stream()
                .map(this::getLastOf)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(platformBuilder -> platformBuilder.getDeployedModuleBuilders()
                        .stream()
                        .anyMatch(deployedModuleBuilder -> deployedModuleBuilder.equalsByKey(moduleToLookFor)))
                .map(platformBuilder -> new ModulePlatformsOutput(platformBuilder.getApplicationName(), platformBuilder.getPlatformName()))
                .collect(Collectors.toList());
    }

    public PlatformBuilder getPlatformByName(String platformName) {
        List<PlatformBuilder> matchingPlatforms = platformBuilders.entrySet().stream()
                .filter(entry -> entry.getKey().getPlatformName().equals(platformName))
                .map(Map.Entry::getValue)
                .map(this::getLastOf)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(platformBuilder -> platformBuilder.getPlatformName().equals(platformName))
                .collect(Collectors.toList());
        if (matchingPlatforms.size() != 1) {
            throw new RuntimeException("Incorrect matching platforms count: " + matchingPlatforms.size());
        }
        return matchingPlatforms.get(0);
    }

    public ApplicationOutput buildApplicationOutput(boolean withoutModules) {
        return buildApplicationOutput(platformBuilders.get(0).getApplicationName(), platformBuilders, withoutModules);
    }

    private static ApplicationOutput buildApplicationOutput(String applicationName, List<PlatformBuilder> platformBuilders, boolean withoutModules) {
        return new ApplicationOutput(applicationName,
                platformBuilders.stream()
                        .filter(platformBuilder -> platformBuilder.getApplicationName().equals(applicationName))
                        .map(platformBuilder -> platformBuilder.buildOutput(withoutModules))
                        .collect(Collectors.toList()),
                new HashMap<>());
    }

    public List<ApplicationOutput> buildApplicationOutputs() {
        Map<String, List<PlatformBuilder>> applicationPlatformsMap = platformBuilders.stream()
                .collect(Collectors.groupingBy(PlatformBuilder::getApplicationName));

        return applicationPlatformsMap.entrySet().stream()
                .map(entry -> buildApplicationOutput(entry.getKey(), entry.getValue(), false))
                .collect(Collectors.toList());
    }
}
