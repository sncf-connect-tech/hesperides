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

import org.hesperides.core.presentation.io.platforms.ModulePlatformsOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.PropertiesIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OldPlatformHistory {

    @Autowired
    private OldPlatformBuilder oldPlatformBuilder;

    private Map<Long, PlatformIO> platforms = new HashMap<>();
    private Map<Long, PropertiesIO> platformProperties = new HashMap<>();

    public void reset() {
        platforms = new HashMap<>();
        platformProperties = new HashMap<>();
    }

    public void addPlatform() {
        Long timestamp = System.currentTimeMillis();
        platforms.put(timestamp, oldPlatformBuilder.buildInput());
        platformProperties.put(timestamp, oldPlatformBuilder.getPropertiesIO(false));
    }

    public Long getFirstPlatformTimestamp() {
        return platforms.keySet().stream().sorted().findFirst().get();
    }

    public PlatformIO getInitialPlatformState() {
        return platforms.get(getFirstPlatformTimestamp());
    }

    public PropertiesIO getInitialPlatformProperties() {
        return platformProperties.get(getFirstPlatformTimestamp());
    }

    public List<ModulePlatformsOutput> buildModulePlatforms() {
        return platforms.keySet().stream().sorted().map(t -> platforms.get(t))
                .map(platform -> new ModulePlatformsOutput(platform.getApplicationName(), platform.getPlatformName()))
                .collect(Collectors.toList());
    }

    public List<PlatformIO> getPlatformsSortedByTimestamp() {
        return platforms.keySet().stream().sorted().map(t -> platforms.get(t)).collect(Collectors.toList());
    }

    public PlatformIO getPlatformByName(String platformName) {
        List<PlatformIO> matchingPlatforms = platforms.values().stream()
                .filter(p -> p.getPlatformName().equals(platformName))
                .collect(Collectors.toList());
        if (matchingPlatforms.size() != 1) {
            throw new RuntimeException("Incorrect matching platforms count: " + matchingPlatforms.size());
        }
        return matchingPlatforms.get(0);
    }
}
