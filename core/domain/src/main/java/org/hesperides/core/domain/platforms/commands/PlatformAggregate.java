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

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Slf4j
@Aggregate
@NoArgsConstructor
public class PlatformAggregate implements Serializable {

    @AggregateIdentifier
    private Platform.Key key;
    private Long versionId;

    /*** COMMAND HANDLERS ***/

    @CommandHandler
    public PlatformAggregate(CreatePlatformCommand command) {
        //TODO Logs
        // Initialise le versionId de la plateforme et l'identifiant et le propertiesPath des modules de la plateforme
        Platform platform = command.getPlatform()
                .initializeVersionId()
                .updateDeployedModules();

        apply(new PlatformCreatedEvent(platform, command.getUser()));
    }


    @CommandHandler
    public void onUpdatePlatformCommand(UpdatePlatformCommand command) {

        // TODO populate properties when `cmd.copyProps` flag is set (-> dedicated aggregate / command ?)

        Platform platform = command.getPlatform()
                .validateVersionId(versionId)
                .incrementVersionId()
                .updateDeployedModules();

        apply(new PlatformUpdatedEvent(command.getPlatformKey(), platform, command.getUser()));
    }

    @CommandHandler
    public void onDeletePlatformCommand(DeletePlatformCommand command) {
        apply(new PlatformDeletedEvent(command.getPlatformKey(), command.getUser()));
    }

    @CommandHandler
    public void onUpdatePlatformModulePropertiesCommand(UpdatePlatformModulePropertiesCommand command) {
        if (command.getPlatformVersionId() != versionId) {
            throw new OutOfDateVersionException(command.getPlatformVersionId(), versionId);
        }
        apply(new PlatformModulePropertiesUpdatedEvent(command.getPlatformKey(), command.getModulePath(), command.getPlatformVersionId()+1, command.getValuedProperties(), command.getUser()));
    }

    @CommandHandler
    public void onUpdatePlatformPropertiesCommand(UpdatePlatformPropertiesCommand command) {
        if (command.getPlatformVersionId() != versionId) {
            throw new OutOfDateVersionException(command.getPlatformVersionId(), versionId);
        }
        apply(new PlatformPropertiesUpdatedEvent(command.getPlatformKey(), command.getPlatformVersionId()+1, command.getValuedProperties(), command.getUser()));
    }

    /*** EVENT HANDLERS ***/

    @EventSourcingHandler
    public void onPlatformCreatedEvent(PlatformCreatedEvent event) {
        this.key = event.getPlatform().getKey();
        this.versionId = event.getPlatform().getVersionId();
        log.debug("Plateform created");
    }

    @EventSourcingHandler
    public void onPlatformUpdatedEvent(PlatformUpdatedEvent event) {
        this.versionId = event.getPlatform().getVersionId();
        log.debug("Platform updated");
    }

    @EventSourcingHandler
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        log.debug("Platform deleted");
    }

    @EventSourcingHandler
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {
        this.key = event.getPlatformKey();
        this.versionId = event.getPlatformVersionId();
        log.debug("Plaform module {} updated with properties {}", event.getModulePath(), event.getValuedProperties());
    }

    @EventSourcingHandler
    public void onPlatformPropertiesUpdatedEvent(PlatformPropertiesUpdatedEvent event) {
        this.key = event.getPlatformKey();
        this.versionId = event.getPlatformVersionId();
        log.debug("Plaform updated with properties {}", event.getValuedProperties());
    }
}
