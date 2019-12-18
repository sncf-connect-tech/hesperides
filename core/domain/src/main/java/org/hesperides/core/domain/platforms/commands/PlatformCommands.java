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

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.security.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlatformCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public PlatformCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public String createPlatform(Platform platform, User user) {
        return commandGateway.sendAndWait(new CreatePlatformCommand(platform, user));
    }

    public void updatePlatform(String platformId, Platform platform, boolean copyPropertiesForUpgradedModules, User user) {
        // Debug note: .sendAndWait can raise AggregateNotFoundException("The aggregate was not found in the event store")
        // It probably means the event store and the projection repository are desynchronized
        commandGateway.sendAndWait(new UpdatePlatformCommand(platformId, platform, copyPropertiesForUpgradedModules, user));
    }

    public void deletePlatform(String platformId, Platform.Key platformKey, User user) {
        commandGateway.sendAndWait(new DeletePlatformCommand(platformId, platformKey, user));
    }

    public void saveModulePropertiesInPlatform(final String platformId,
                                               final String propertiesPath,
                                               final Long platformVersionId,
                                               final Long propertiesVersionId,
                                               final Long expectedPropertiesVersionId,
                                               final List<AbstractValuedProperty> valuedProperties,
                                               final String userComment,
                                               final User user) {
        commandGateway.sendAndWait(new UpdatePlatformModulePropertiesCommand(platformId, propertiesPath, platformVersionId, propertiesVersionId, expectedPropertiesVersionId, valuedProperties, userComment, user));
    }

    public void savePlatformProperties(final String platformId,
                                       final Long platformVersionId,
                                       final Long propertiesVersionId,
                                       final Long expectedPropertiesVersionId,
                                       final List<ValuedProperty> valuedProperties,
                                       final User user) {
        commandGateway.sendAndWait(new UpdatePlatformPropertiesCommand(platformId, platformVersionId, propertiesVersionId, expectedPropertiesVersionId, valuedProperties, user));
    }

    public void restoreDeletedPlatform(final String platformId, final User user) {
        commandGateway.sendAndWait(new RestoreDeletedPlatformCommand(platformId, user));
    }
}
