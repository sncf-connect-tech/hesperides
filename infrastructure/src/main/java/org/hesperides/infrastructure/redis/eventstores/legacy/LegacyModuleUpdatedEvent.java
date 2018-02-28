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
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.hesperides.domain.modules.ModuleUpdatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Techno;

import java.util.ArrayList;
import java.util.Collection;

@Value
public class LegacyModuleUpdatedEvent {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyUpdatedEvent";

    LegacyModule moduleUpdated;
    Collection templates;

    /**
     * Mapping d'un évènement de la nouvelle application en évènement legacy
     */
    public static String fromDomainEvent(ModuleUpdatedEvent domainEvent) {
        Module.Key moduleKey = domainEvent.getModuleKey();
        LegacyModule legacyModule = new LegacyModule(
                moduleKey.getName(),
                moduleKey.getVersion(),
                moduleKey.isWorkingCopy(),
                new ArrayList(), // Toujours une liste de technos vide lors de la création d'un module
                domainEvent.getModule().getVersionID());
        return new Gson().toJson(new LegacyModuleUpdatedEvent(legacyModule, new ArrayList()));
    }

    /**
     * Mapping du json legacy vers un évènement du domaine de la nouvelle application
     *
     * @param jsonData
     * @param aggregateIdentifier
     * @param firstSequenceNumber
     * @return
     */
    public static DomainEventMessage<ModuleUpdatedEvent> toDomainEventMessage(String jsonData, String aggregateIdentifier, long firstSequenceNumber) {
        LegacyModuleUpdatedEvent legacyModuleUpdatedEvent = new Gson().fromJson(jsonData, LegacyModuleUpdatedEvent.class);
        ModuleUpdatedEvent moduleUpdatedEvent = legacyModuleUpdatedEvent.toDomainEvent();
        return new GenericDomainEventMessage(ModuleUpdatedEvent.class.getName(), aggregateIdentifier, firstSequenceNumber, moduleUpdatedEvent);
    }

    private ModuleUpdatedEvent toDomainEvent() {
        LegacyModule legacyModule = this.getModuleUpdated();
        Module.Key moduleKey = new Module.Key(
                legacyModule.getName(),
                legacyModule.getVersion(),
                legacyModule.isWorking_copy() ? Module.Type.workingcopy : Module.Type.release);
        Module module = new Module(moduleKey, new ArrayList<>(), legacyModule.getVersion_id());
        return new ModuleUpdatedEvent(moduleKey, module);
    }

}
