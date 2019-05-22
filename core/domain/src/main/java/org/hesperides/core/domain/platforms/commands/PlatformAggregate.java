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
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.platforms.*;
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
        log.debug("PlatformAggregate constructor - platformId: %s - key: %s - versionId: %s - user: %s",
                newUuid, command.getPlatform().getKey(), command.getPlatform().getVersionId(), command.getUser());
        apply(new PlatformCreatedEvent(newUuid, platform, command.getUser().getName()));
    }


    @CommandHandler
    public void onUpdatePlatformCommand(UpdatePlatformCommand command) {
        log.debug("onUpdatePlatformCommand - platformId: %s - key: %s - versionId: %s - user: %s",
                command.getPlatformId(), command.getPlatform().getKey(), command.getPlatform().getVersionId(), command.getUser());
        Platform platform = command.getPlatform()
                .validateVersionId(versionId)
                .validateDeployedModulesDistinctIds()
                .incrementVersionId()
                .fillDeployedModulesMissingIds();

        apply(new PlatformUpdatedEvent(command.getPlatformId(), platform, command.getCopyPropertiesForUpgradedModules(), command.getUser().getName()));
    }

    @CommandHandler
    public void onDeletePlatformCommand(DeletePlatformCommand command) {
        log.debug("onDeletePlatformCommand - platformId: %s - user: %s",
                command.getPlatformId(), command.getUser());
        apply(new PlatformDeletedEvent(command.getPlatformId(), command.getPlatformKey(), command.getUser().getName()));
    }

    @CommandHandler
    public void onUpdatePlatformModulePropertiesCommand(UpdatePlatformModulePropertiesCommand command) {
        log.debug("onUpdatePlatformModulePropertiesCommand - platformId: %s - versionId: %s - user: %s",
                command.getPlatformId(), command.getPlatformVersionId(), command.getUser());
        if (command.getPlatformVersionId() != versionId) {
            throw new OutOfDateVersionException(versionId, command.getPlatformVersionId());
        }
        apply(new PlatformModulePropertiesUpdatedEvent(
                command.getPlatformId(),
                command.getPropertiesPath(),
                (command.getPlatformVersionId() + 1),
                command.getValuedProperties(),
                command.getUser().getName()));
    }

    @CommandHandler
    public void onUpdatePlatformPropertiesCommand(UpdatePlatformPropertiesCommand command) {
        log.debug("onUpdatePlatformPropertiesCommand - platformId: %s - versionId: %s - user: %s",
                command.getPlatformId(), command.getPlatformVersionId(), command.getUser());
        if (command.getPlatformVersionId() != versionId) {
            throw new OutOfDateVersionException(versionId, command.getPlatformVersionId());
        }
        apply(new PlatformPropertiesUpdatedEvent(
                command.getPlatformId(),
                (command.getPlatformVersionId() + 1),
                command.getValuedProperties(),
                command.getUser().getName()));
    }

    @CommandHandler
    public void onRestoreDeletedPlatformCommand(RestoreDeletedPlatformCommand command) {
        log.debug("onRestoreDeletedPlatformCommand - platformId: %s - user: %s",
                command.getPlatformId(), command.getUser());
        apply(new RestoreDeletedPlatformEvent(
                command.getPlatformId(),
                command.getUser().getName()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    public void onPlatformCreatedEvent(PlatformCreatedEvent event) {
        log.debug("onPlatformCreatedEvent - platformId: %s - key: %s - versionId: %s - user: %s",
                event.getPlatformId(), event.getPlatformKey(), event.getPlatform().getVersionId(), event.getUser());
        this.id = event.getPlatformId();
        this.key = event.getPlatformKey();
        this.versionId = event.getPlatform().getVersionId();
    }

    @EventSourcingHandler
    public void onPlatformUpdatedEvent(PlatformUpdatedEvent event) {
        log.debug("onPlatformCreatedEvent - platformId: %s - key: %s - versionId: %s - user: %s",
                event.getPlatformId(), event.getPlatformKey(), event.getPlatform().getVersionId(), event.getUser());
        this.versionId = event.getPlatform().getVersionId();
    }

    @EventSourcingHandler
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        log.debug("onPlatformDeletedEvent - platformId: %s - key: %s - user: %s",
                event.getPlatformId(), event.getPlatformKey(), event.getUser());
    }

    @EventSourcingHandler
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {
        log.debug("onPlatformModulePropertiesUpdatedEvent - platformId: %s - versionId: %s - user: %s",
                event.getPlatformId(), event.getPlatformVersionId(), event.getUser());
        this.versionId = event.getPlatformVersionId();
    }

    @EventSourcingHandler
    public void onPlatformPropertiesUpdatedEvent(PlatformPropertiesUpdatedEvent event) {
        log.debug("onPlatformPropertiesUpdatedEvent - platformId: %s - versionId: %s - user: %s",
                event.getPlatformId(), event.getPlatformVersionId(), event.getUser());
        this.versionId = event.getPlatformVersionId();
    }
}
