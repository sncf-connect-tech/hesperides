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

import lombok.Data;
import lombok.Value;
import org.apache.commons.lang3.SerializationUtils;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.presentation.io.platforms.ApplicationOutput;
import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.test.bdd.platforms.builders.DeployedModuleBuilder;
import org.hesperides.test.bdd.platforms.builders.PlatformBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PlatformHistory {

    private List<PlatformTimestampedBuilders> platforms;

    public PlatformHistory() {
        reset();
    }

    public PlatformHistory reset() {
        platforms = new ArrayList<>();
        return this;
    }

    public void addPlatformBuilder(PlatformBuilder platformBuilder) {
        if (platforms.stream().anyMatch(platform -> !platform.isDeleted && platform.getPlatformKey().equals(platformBuilder.buildPlatformKey()))) {
            throw new RuntimeException("Platform " + platformBuilder.getApplicationName() + "-" + platformBuilder.getPlatformName() + " already exists in platform history");
        }
        PlatformBuilder newPlatformBuilder = SerializationUtils.clone(platformBuilder);
        platforms.add(new PlatformTimestampedBuilders(newPlatformBuilder));
    }

    public void removePlatformBuilder(PlatformBuilder platformBuilder) {
        if (platforms.stream().noneMatch(platform -> platform.getPlatformKey().equals(platformBuilder.buildPlatformKey()))) {
            throw new RuntimeException("Can't remove platform " + platformBuilder.getApplicationName() + "-" + platformBuilder.getPlatformName() + " because it doesn't exist in platform history");
        }
        platforms.forEach(platform -> {
            if (platform.getPlatformKey().equals(platformBuilder.buildPlatformKey())) {
                platform.setDeleted(true);
            }
        });
    }

    public void updatePlatformBuilder(PlatformBuilder platformBuilder) {
        if (platforms.stream().noneMatch(platform -> platform.getPlatformKey().equals(platformBuilder.buildPlatformKey()))) {
            throw new RuntimeException("Can't upadte platform " + platformBuilder.getApplicationName() + "-" + platformBuilder.getPlatformName() + " because it doesn't exist in platform history");
        }

        platformBuilder.incrementVersionId();
        platformBuilder.setDeployedModuleIds();
        PlatformBuilder updatedPlatformBuilder = SerializationUtils.clone(platformBuilder);

        TimestampedBuilder timestampedBuilder = new TimestampedBuilder(updatedPlatformBuilder);
        platforms.forEach(platform -> {
            if (platform.getPlatformKey().equals(platformBuilder.buildPlatformKey())) {
                platform.setDeleted(false);
                platform.getTimestampedBuilders().add(timestampedBuilder);
            }
        });
    }

    public List<ModulePlatformsOutput> buildModulePlatforms(DeployedModuleBuilder moduleToLookFor) {
        return platforms.stream()
                .filter(platform -> !platform.isDeleted)
                .map(PlatformTimestampedBuilders::getTimestampedBuilders)
                .map(PlatformHistory::getLastPlatformBuilder)
                .filter(platformBuilder -> platformBuilder.getDeployedModuleBuilders()
                        .stream()
                        .anyMatch(deployedModuleBuilder -> deployedModuleBuilder.equalsByKey(moduleToLookFor)))
                .map(platformBuilder -> new ModulePlatformsOutput(platformBuilder.getApplicationName(), platformBuilder.getPlatformName()))
                .collect(Collectors.toList());
    }

    private static PlatformBuilder getLastPlatformBuilder(List<TimestampedBuilder> timestampedBuilders) {
        return timestampedBuilders.stream()
                .max(Comparator.comparing(TimestampedBuilder::getTimestamp))
                .orElseThrow(() -> new RuntimeException("Can't get last platform builder from timestamped builders"))
                .getPlatformBuilder();
    }

    public PlatformBuilder getPlatformByName(String platformName) {
        List<PlatformBuilder> matchingPlatforms = platforms.stream()
                .filter(platform -> platform.getPlatformKey().getPlatformName().equals(platformName))
                .map(PlatformTimestampedBuilders::getTimestampedBuilders)
                .map(PlatformHistory::getLastPlatformBuilder)
                .collect(Collectors.toList());

        if (matchingPlatforms.size() != 1) {
            throw new RuntimeException("Incorrect matching platforms count: " + matchingPlatforms.size());
        }
        return matchingPlatforms.get(0);
    }

    public ApplicationOutput buildApplicationOutput(String applicationName, boolean withoutModules) {
        List<PlatformBuilder> platformBuilders = platforms.stream()
                .filter(platform -> platform.getPlatformKey().getApplicationName().equals(applicationName)
                        && !platform.isDeleted)
                .map(PlatformTimestampedBuilders::getTimestampedBuilders)
                .map(PlatformHistory::getLastPlatformBuilder)
                .collect(Collectors.toList());

        return buildApplicationOutput(
                applicationName,
                platformBuilders,
                withoutModules);
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
        Map<String, List<PlatformBuilder>> applicationPlatformsMap = platforms.stream()
                .filter(platform -> !platform.isDeleted)
                .map(PlatformTimestampedBuilders::getTimestampedBuilders)
                .map(PlatformHistory::getLastPlatformBuilder)
                .collect(Collectors.groupingBy(PlatformBuilder::getApplicationName));

        return applicationPlatformsMap.entrySet().stream()
                .map(entry -> buildApplicationOutput(entry.getKey(), entry.getValue(), false))
                .collect(Collectors.toList());
    }

    public Long getPlatformFirstTimestamp(String applicationName, String platformName) {
        return getPlatformTimestampedBuilderAtIndex(applicationName, platformName, 0).getTimestamp();
    }

    public PlatformBuilder getFirstPlatformBuilder(String applicationName, String platformName) {
        return getPlatformTimestampedBuilderAtIndex(applicationName, platformName, 0).getPlatformBuilder();
    }

    public PlatformBuilder getSecondPlatformBuilder(String applicationName, String platformName) {
        return getPlatformTimestampedBuilderAtIndex(applicationName, platformName, 1).getPlatformBuilder();
    }

    private TimestampedBuilder getPlatformTimestampedBuilderAtIndex(String applicationName, String platformName, int index) {
        return platforms.stream()
                .filter(platform -> platform.getPlatformKey().getApplicationName().equals(applicationName) &&
                        platform.getPlatformKey().getPlatformName().equals(platformName))
                .findFirst()
                .map(PlatformTimestampedBuilders::getTimestampedBuilders)
                .map(timestampedBuilders -> timestampedBuilders.get(index))
                .orElseThrow(() -> new RuntimeException("Can't find platform " + applicationName + "-" + platformName));
    }

    public Long getPenultimatePlatformTimestamp(String applicationName, String platformName) {
        return getPenultimatePlatformTimestampBuilder(applicationName, platformName).getTimestamp();
    }

    // penultimate = second to last = avant dernier
    private TimestampedBuilder getPenultimatePlatformTimestampBuilder(String applicationName, String platformName) {
        return platforms.stream()
                .filter(platform -> platform.getPlatformKey().getApplicationName().equals(applicationName) &&
                        platform.getPlatformKey().getPlatformName().equals(platformName))
                .findFirst()
                .map(PlatformTimestampedBuilders::getTimestampedBuilders)
                .map(timestampedBuilders -> timestampedBuilders.get(timestampedBuilders.size() - 2))
                .orElseThrow(() -> new RuntimeException("Can't find platform " + applicationName + "-" + platformName));
    }

    @Data
    private static class PlatformTimestampedBuilders {
        private Platform.Key platformKey;
        private List<TimestampedBuilder> timestampedBuilders;
        private boolean isDeleted;

        private PlatformTimestampedBuilders(PlatformBuilder platformBuilder) {
            platformKey = platformBuilder.buildPlatformKey();
            timestampedBuilders = new ArrayList<>();
            timestampedBuilders.add(new TimestampedBuilder(platformBuilder));
            isDeleted = false;
        }
    }

    @Value
    private static class TimestampedBuilder {
        Long timestamp;
        PlatformBuilder platformBuilder;

        private TimestampedBuilder(PlatformBuilder platformBuilder) {
            // Les tests faisant référence au timestamp d'un événement passé plantent
            // de manière aléatoire en local. Cela vient du fait que le timestamp
            // sauvegardé dans `PlatformHistory` est parfois défini juste avant la fin
            // de la sauvegarde effective de l'événement. En attendant de trouver mieux...
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            timestamp = System.currentTimeMillis();
            this.platformBuilder = platformBuilder;
        }
    }
}
