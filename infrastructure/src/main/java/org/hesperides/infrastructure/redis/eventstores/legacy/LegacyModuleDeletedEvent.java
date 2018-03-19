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
package org.hesperides.infrastructure.redis.eventstores.legacy;

import com.google.gson.Gson;
import com.sun.org.apache.xpath.internal.operations.Mod;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.hesperides.domain.modules.ModuleDeletedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;
import org.hesperides.domain.security.UserEvent;

import java.util.ArrayList;
import java.util.Collection;

@Value
@EqualsAndHashCode(callSuper = true)
public class LegacyModuleDeletedEvent extends AbstractLegacyEvent {

    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleDeletedEvent";

    String moduleName;
    String moduleVersion;
    boolean workingCopy;

    /**
     * Mapping d'un évènement de la nouvelle application en évènement legacy
     *
     * @param domainEventMessage
     */
    public static String fromDomainEventMessage(DomainEventMessage domainEventMessage) {
        ModuleDeletedEvent domainEvent = (ModuleDeletedEvent) domainEventMessage.getPayload();
        Module.Key moduleKey = domainEvent.getModule().getKey();
        return new Gson().toJson(new LegacyModuleDeletedEvent(moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy()));
    }

    public static DomainEventMessage<? extends UserEvent> toDomainEventMessage(LegacyEvent legacyEvent, String aggregateIdentifier, long sequenceNumber) {
        return legacyEvent.toDomainEventMessage(aggregateIdentifier, sequenceNumber, LegacyModuleDeletedEvent.class, ModuleDeletedEvent.class);
    }

    @Override
    protected ModuleDeletedEvent toDomainEvent(String username) {
        Module.Key moduleKey = new Module.Key(
                getModuleName(),
                getModuleVersion(),
                isWorkingCopy() ? Module.Type.workingcopy : Module.Type.release);
        Module module = new Module(moduleKey, new ArrayList<>(), Long.MIN_VALUE);
        return new ModuleDeletedEvent(module, new User(username));
    }

}
