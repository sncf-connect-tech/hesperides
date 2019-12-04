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
package org.hesperides.core.domain.keyvalues.commands;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hesperides.core.domain.keyvalues.CreateKeyValueCommand;
import org.hesperides.core.domain.keyvalues.DeleteKeyValueCommand;
import org.hesperides.core.domain.keyvalues.UpdateKeyValueCommand;
import org.hesperides.core.domain.keyvalues.entities.KeyValue;
import org.hesperides.core.domain.security.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyValueCommands {

    private final CommandGateway commandGateway;

    @Autowired
    public KeyValueCommands(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    public String createKeyValue(KeyValue keyValue, User user) {
        return commandGateway.sendAndWait(new CreateKeyValueCommand(keyValue, user));
    }

    public void updateKeyValue(String id, KeyValue keyValue, User user) {
        commandGateway.sendAndWait(new UpdateKeyValueCommand(id, keyValue, user));
    }

    public void deleteKeyValue(String id, User user) {
        commandGateway.sendAndWait(new DeleteKeyValueCommand(id, user));
    }
}
