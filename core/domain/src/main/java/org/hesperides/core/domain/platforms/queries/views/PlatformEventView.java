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
package org.hesperides.core.domain.platforms.queries.views;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.platforms.PlatformCreatedEvent;
import org.hesperides.core.domain.platforms.PlatformUpdatedEvent;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Value
public class PlatformEventView {

    Instant timestamp;
    List<PlatformChangeView> changes;

    @Value
    @NonFinal
    public static class PlatformChangeView {
    }

    @Value
    public static class PlatformCreatedView extends PlatformChangeView {
    }

    @Value
    public static class PlatformVersionUpdatedView extends PlatformChangeView {
        String oldVersion;
        String newVersion;
    }

    @Value
    public static class DeployedModuleUpdatedView extends PlatformChangeView {
        String oldPropertiesPath;
        String newPropertiesPath;
    }

    @Value
    public static class DeployedModuleAddedView extends PlatformChangeView {
        String propertiesPath;
    }

    @Value
    public static class DeployedModuleRemovedView extends PlatformChangeView {
        String propertiesPath;
    }

    public static List<PlatformEventView> buildPlatformEvents(List<EventView> events, Integer page, Integer size) {
        List<PlatformEventView> platformEvents = new ArrayList<>();

        ListIterator<EventView> eventsIterator = events.listIterator();
        while (eventsIterator.hasNext()) {
            EventView previousEvent = eventsIterator.next();
            if (isPlatformCreatedEvent(previousEvent)) {
                // Evènement de création
                platformEvents.add(new PlatformEventView(previousEvent.getTimestamp(), Collections.singletonList(new PlatformCreatedView())));
            }

            if (eventsIterator.hasNext()) {
                List<PlatformChangeView> platformChanges = new ArrayList<>();
                EventView currentEvent = eventsIterator.next();
                Platform previousPlatformData = getPlatformDataFromCreateOrUpdateEvent(previousEvent);
                Platform currentPlatformData = getPlatformDataFromCreateOrUpdateEvent(currentEvent);

                if (!previousPlatformData.getVersion().equals(currentPlatformData.getVersion())) {
                    // Mise à jour de la version de la plateforme
                    platformChanges.add(new PlatformVersionUpdatedView(previousPlatformData.getVersion(), currentPlatformData.getVersion()));
                }

                List<DeployedModule> previousDeployedModules = previousPlatformData.getDeployedModules();
                List<DeployedModule> currentDeployedModules = currentPlatformData.getDeployedModules();
                Map<Long, DeployedModule> currentDeployedModulesById = currentDeployedModules.stream().collect(toMap(DeployedModule::getId, identity()));
                Map<Long, DeployedModule> previousDeployedModulesById = previousDeployedModules.stream().collect(toMap(DeployedModule::getId, identity()));

                previousDeployedModules.forEach(previousDeployedModule -> {
                    if (currentDeployedModulesById.containsKey(previousDeployedModule.getId())) {
                        DeployedModule currentDeployedModule = currentDeployedModulesById.get(previousDeployedModule.getId());
                        // Mise à jour du module déployé
                        if (!previousDeployedModule.getPropertiesPath().equals(currentDeployedModule.getPropertiesPath())) {
                            platformChanges.add(new DeployedModuleUpdatedView(previousDeployedModule.getPropertiesPath(), currentDeployedModule.getPropertiesPath()));
                        }
                    } else {
                        // Module déployé supprimé
                        platformChanges.add(new DeployedModuleRemovedView(previousDeployedModule.getPropertiesPath()));
                    }
                });

                currentDeployedModules.forEach(currentDeployedModule -> {
                    if (!previousDeployedModulesById.containsKey(currentDeployedModule.getId())) {
                        // Nouveau module déployé
                        platformChanges.add(new PlatformEventView.DeployedModuleAddedView(currentDeployedModule.getPropertiesPath()));
                    }
                });

                if (!CollectionUtils.isEmpty(platformChanges)) {
                    platformEvents.add(new PlatformEventView(currentEvent.getTimestamp(), platformChanges));
                }
                // Nécessaire pour repartir de la plateforme en cours
                eventsIterator.previous();
            }
        }
        return platformEvents
                .stream()
                .skip((page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    private static Platform getPlatformDataFromCreateOrUpdateEvent(EventView event) {
        Platform platformData;
        if (isPlatformCreatedEvent(event)) {
            platformData = ((PlatformCreatedEvent) event.getData()).getPlatform();
        } else {
            platformData = ((PlatformUpdatedEvent) event.getData()).getPlatform();
        }
        return platformData;
    }

    private static boolean isPlatformCreatedEvent(EventView event) {
        return event.getData() instanceof PlatformCreatedEvent;
    }
}
