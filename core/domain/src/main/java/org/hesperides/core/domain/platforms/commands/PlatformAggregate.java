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
package org.hesperides.core.domain.platforms.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.commons.VersionIdLogger;
import org.hesperides.core.domain.exceptions.OutOfDateGlobalPropertiesException;
import org.hesperides.core.domain.exceptions.OutOfDatePlatformVersionException;
import org.hesperides.core.domain.exceptions.OutOfDatePropertiesException;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;

import java.io.Serializable;
import java.util.UUID;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@Aggregate(snapshotTriggerDefinition = "snapshotTrigger")
public class PlatformAggregate implements Serializable {

    @AggregateIdentifier
    private String id;
    private Platform.Key key;
    private Long versionId;

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    public PlatformAggregate(CreatePlatformCommand command) {
        // Initialise le versionId de la plateforme et l'identifiant et le propertiesPath des modules de la plateforme
        Platform platform = command.getPlatform()
                .validateDeployedModulesDistinctIds()
                .initializeVersionId()
                .fillDeployedModulesMissingIds();
        String newUuid = UUID.randomUUID().toString();
        log.debug("PlatformAggregate constructor - platformId: {} - key: {} - versionId: {} - user: {}",
                newUuid, command.getPlatform().getKey(), command.getPlatform().getVersionId(), command.getUser());
        logBeforeEventVersionId(command.getPlatform().getVersionId());
        apply(new PlatformCreatedEvent(newUuid, platform, command.getUser().getName()));
    }


    @CommandHandler
    public void onUpdatePlatformCommand(UpdatePlatformCommand command) {
        log.debug("onUpdatePlatformCommand - platformId: {} - key: {} - versionId: {} - user: {}",
                command.getPlatformId(), command.getPlatform().getKey(), command.getPlatform().getVersionId(), command.getUser());
        logBeforeEventVersionId(command.getPlatform().getVersionId());
        Platform platform = command.getPlatform()
                .validateVersionId(versionId)
                .validateDeployedModulesDistinctIds()
                .incrementVersionId()
                .fillDeployedModulesMissingIds();
        apply(new PlatformUpdatedEvent(command.getPlatformId(), platform, command.getCopyPropertiesForUpgradedModules(), command.getUser().getName()));
    }

    @CommandHandler
    public void onDeletePlatformCommand(DeletePlatformCommand command) {
        log.debug("onDeletePlatformCommand - platformId: {} - user: {}",
                command.getPlatformId(), command.getUser());
        logBeforeEventVersionId();
        apply(new PlatformDeletedEvent(command.getPlatformId(), command.getPlatformKey(), command.getUser().getName()));
    }

    @CommandHandler
    public void onUpdatePlatformModulePropertiesCommand(UpdatePlatformModulePropertiesCommand command) {
        Long platformVersionId = command.getPlatformVersionId();

        log.debug("onUpdatePlatformModulePropertiesCommand - platformId: {} - versionId: {} - user: {}",
                command.getPlatformId(), platformVersionId, command.getUser());
        logBeforeEventVersionId(platformVersionId);

        checkVersionIds(platformVersionId, command.getPropertiesVersionId(), command.getExpectedPropertiesVersionId(), command.getPropertiesPath(), false);

        apply(new PlatformModulePropertiesUpdatedEvent(
                command.getPlatformId(),
                command.getPropertiesPath(),
                (versionId + 1),
                (command.getExpectedPropertiesVersionId() + 1),
                command.getValuedProperties(),
                command.getUser().getName()));
    }

    @CommandHandler
    public void onUpdatePlatformPropertiesCommand(UpdatePlatformPropertiesCommand command) {
        Long platformVersionId = command.getPlatformVersionId();
        log.debug("onUpdatePlatformPropertiesCommand - platformId: {} - versionId: {} - user: {}",
                command.getPlatformId(), platformVersionId, command.getUser());
        logBeforeEventVersionId(platformVersionId);

        checkVersionIds(platformVersionId, command.getPropertiesVersionId(), command.getExpectedPropertiesVersionId(), true);

        apply(new PlatformPropertiesUpdatedEvent(
                command.getPlatformId(),
                (versionId + 1),
                (command.getExpectedPropertiesVersionId() + 1),
                command.getValuedProperties(),
                command.getUser().getName()));
    }

    @CommandHandler
    public void onRestoreDeletedPlatformCommand(RestoreDeletedPlatformCommand command) {
        log.debug("onRestoreDeletedPlatformCommand - platformId: {} - user: {}",
                command.getPlatformId(), command.getUser());
        logBeforeEventVersionId();
        apply(new RestoreDeletedPlatformEvent(
                command.getPlatformId(),
                command.getUser().getName()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    public void onPlatformCreatedEvent(PlatformCreatedEvent event) {
        log.debug("onPlatformCreatedEvent - platformId: {} - key: {} - versionId: {} - user: {}",
                event.getPlatformId(), event.getPlatformKey(), event.getPlatform().getVersionId(), event.getUser());
        logAfterEventVersionId(event.getPlatform().getVersionId());
        this.id = event.getPlatformId();
        this.key = event.getPlatformKey();
        this.versionId = event.getPlatform().getVersionId();
    }

    @EventSourcingHandler
    public void onPlatformUpdatedEvent(PlatformUpdatedEvent event) {
        log.debug("onPlatformCreatedEvent - platformId: {} - key: {} - versionId: {} - user: {}",
                event.getPlatformId(), event.getPlatformKey(), event.getPlatform().getVersionId(), event.getUser());
        logAfterEventVersionId(event.getPlatform().getVersionId());
        this.versionId = event.getPlatform().getVersionId();
    }

    @EventSourcingHandler
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        log.debug("onPlatformDeletedEvent - platformId: {} - key: {} - user: {}",
                event.getPlatformId(), event.getPlatformKey(), event.getUser());
        logAfterEventVersionId();
    }

    @EventSourcingHandler
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {
        log.debug("onPlatformModulePropertiesUpdatedEvent - platformId: {} - versionId: {} - user: {}",
                event.getPlatformId(), event.getPlatformVersionId(), event.getUser());
        logAfterEventVersionId(event.getPlatformVersionId());
        this.versionId = event.getPlatformVersionId();
    }

    @EventSourcingHandler
    public void onPlatformPropertiesUpdatedEvent(PlatformPropertiesUpdatedEvent event) {
        log.debug("onPlatformPropertiesUpdatedEvent - platformId: {} - versionId: {} - user: {}",
                event.getPlatformId(), event.getPlatformVersionId(), event.getUser());
        logAfterEventVersionId(event.getPlatformVersionId());
        this.versionId = event.getPlatformVersionId();
    }


    private void checkVersionIds(Long platformVersionId, Long propertiesVersionId, Long expectedPropertiesVersionId, boolean isGlobal) {
        checkVersionIds(platformVersionId, propertiesVersionId,expectedPropertiesVersionId, null, isGlobal);
    }

    private void checkVersionIds(Long platformVersionId, Long propertiesVersionId, Long expectedPropertiesVersionId, String path, boolean isGlobal) {

        // Dans le cas ou propertiesVersionId et expectedPropertiesVersionId ont tous les deux la valeurs par défaut, deux possibilités:
        // - L'appel a été fait sans fournir de propertiesVersionId
        // - L'appel a été fait avec une valeur initiale pour le propertiesVersionId
        // Dans les deux cas on est dans une situation d'initialisation, le platformVersionId fourni doit être le bon
        if (propertiesVersionId == DeployedModule.INIT_PROPERTIES_VERSION_ID && expectedPropertiesVersionId == propertiesVersionId && platformVersionId != versionId) {
            throw new OutOfDatePlatformVersionException(versionId, platformVersionId);
        }

        // Dans le cas ou le propertiesVersionId est fourni avec une valeur différente par défaut, on ignore le platformVersionId, en contrepartie, les valeurs
        // expectedPropertiesVersionId et propertiesVersionId doivent être identique afin de s'assurer de la consistance des données
        if (propertiesVersionId != DeployedModule.INIT_PROPERTIES_VERSION_ID && expectedPropertiesVersionId != propertiesVersionId) {
            if (isGlobal) {
                throw new OutOfDateGlobalPropertiesException(expectedPropertiesVersionId, platformVersionId);
            } else {
                throw new OutOfDatePropertiesException(path, expectedPropertiesVersionId, propertiesVersionId);
            }
        }
    }

    private void logBeforeEventVersionId() {
        logBeforeEventVersionId(null);
    }

    private void logBeforeEventVersionId(Long entityVersionId) {
        logVersionId(true, entityVersionId);
    }

    private void logAfterEventVersionId() {
        logAfterEventVersionId(null);
    }

    private void logAfterEventVersionId(Long entityVersionId) {
        logVersionId(false, entityVersionId);
    }

    private void logVersionId(boolean isBeforeEvent, Long entityVersionId) {
        VersionIdLogger.log(isBeforeEvent, "platform", id, versionId, entityVersionId);
    }
}
