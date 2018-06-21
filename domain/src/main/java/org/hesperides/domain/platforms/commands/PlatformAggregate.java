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
package org.hesperides.domain.platforms.commands;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.hesperides.domain.platforms.CreatePlatformCommand;
import org.hesperides.domain.platforms.PlatformCreatedEvent;
import org.hesperides.domain.platforms.entities.DeployedModule;
import org.hesperides.domain.platforms.entities.Platform;

import java.io.Serializable;

@Slf4j
@Aggregate
@NoArgsConstructor
public class PlatformAggregate implements Serializable {

    @AggregateIdentifier
    private Platform.Key key;

    @CommandHandler
    public PlatformAggregate(CreatePlatformCommand command) {
        //TODO Logs
        Platform platform = new Platform(
                command.getPlatform().getKey(),
                command.getPlatform().isProductionPlatform(),
                1L,
                DeployedModule.getDeployedModulesWithIdAndPropertiesPath(command.getPlatform().getDeployedModules())
        );

        AggregateLifecycle.apply(new PlatformCreatedEvent(platform, command.getUser()));
    }

    @EventSourcingHandler
    public void on(PlatformCreatedEvent event) {
        this.key = event.getPlatform().getKey();
        log.debug("Plateforme créée");
    }
}
