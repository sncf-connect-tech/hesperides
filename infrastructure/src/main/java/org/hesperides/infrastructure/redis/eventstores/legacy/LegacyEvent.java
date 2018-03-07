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
import org.hesperides.domain.security.UserEvent;

@Value
public class LegacyEvent {
    String eventType;
    String data;
    Long timestamp;
    String user;

    /**
     * Mapping des données du Redis au format JSON vers un évènement du domaine de la nouvelle application
     *
     * @param aggregateIdentifier
     * @param sequenceNumber
     * @param legacyEventClass
     * @param domainEventClass
     * @return
     */
    public DomainEventMessage<? extends UserEvent> toDomainEventMessage(String aggregateIdentifier, long sequenceNumber, Class<? extends AbstractLegacyEvent> legacyEventClass, Class<? extends UserEvent> domainEventClass) {
        AbstractLegacyEvent abstractLegacyEvent = new Gson().fromJson(data, legacyEventClass);
        UserEvent userEvent = abstractLegacyEvent.toDomainEvent(user);
        return new GenericDomainEventMessage(domainEventClass.getName(), aggregateIdentifier, sequenceNumber, userEvent);
    }
}
