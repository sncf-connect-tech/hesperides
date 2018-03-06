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
import lombok.Value;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;

import java.util.ArrayList;
import java.util.Collection;

@Value
public class LegacyModuleUpdatedEvent extends AbstractLegacyEvent {

    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyUpdatedEvent";

    LegacyModule updated;
    Collection templates;

    /**
     * Mapping d'un évènement de la nouvelle application en évènement legacy
     *
     * @param domainEventMessage
     */
    public static String fromDomainEventMessage(DomainEventMessage domainEventMessage) {
        ModuleUpdatedEvent domainEvent = (ModuleUpdatedEvent) domainEventMessage.getPayload();
        Module.Key moduleKey = domainEvent.getModule().getKey();
        LegacyModule legacyModule = new LegacyModule(
                moduleKey.getName(),
                moduleKey.getVersion(),
                moduleKey.isWorkingCopy(),
                new ArrayList(), // Toujours une liste de technos vide lors de la création d'un module
                domainEvent.getModule().getVersionID());
        return new Gson().toJson(new LegacyModuleUpdatedEvent(legacyModule, new ArrayList()));
    }

    public static DomainEventMessage<?> toDomainEventMessage(LegacyEvent legacyEvent, String aggregateIdentifier, long sequenceNumber) {
        return legacyEvent.toDomainEventMessage(aggregateIdentifier, sequenceNumber, LegacyModuleUpdatedEvent.class, ModuleUpdatedEvent.class);
    }

    @Override
    protected ModuleUpdatedEvent toDomainEvent(String username) {
        LegacyModule legacyModule = this.getUpdated();
        Module.Key moduleKey = new Module.Key(
                legacyModule.getName(),
                legacyModule.getVersion(),
                legacyModule.getModuleType());
        Module module = new Module(moduleKey, new ArrayList<>(), legacyModule.getVersionId());
        return new ModuleUpdatedEvent(module, new User(username));
    }

}
