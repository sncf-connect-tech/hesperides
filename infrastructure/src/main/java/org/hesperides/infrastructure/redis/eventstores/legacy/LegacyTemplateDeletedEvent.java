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

import lombok.Value;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.hesperides.domain.modules.TemplateDeletedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.security.User;
import org.hesperides.domain.security.UserEvent;

@Value
public class LegacyTemplateDeletedEvent extends AbstractLegacyEvent {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleTemplateDeletedEvent";

    String moduleName;
    String moduleVersion;
    String templateName;

    public static String fromDomainEventMessage(DomainEventMessage domainEventMessage) {
        TemplateDeletedEvent domainEvent = (TemplateDeletedEvent) domainEventMessage.getPayload();
        Module.Key moduleKey = domainEvent.getModuleKey();
        return LEGACY_GSON_SERIALIZER.toJson(new LegacyTemplateDeletedEvent(moduleKey.getName(), moduleKey.getVersion(), domainEvent.getTemplateName()));
    }

    public static DomainEventMessage<? extends UserEvent> toDomainEventMessage(LegacyEvent legacyEvent, String aggregateIdentifier, long sequenceNumber) {
        return legacyEvent.toDomainEventMessage(aggregateIdentifier, sequenceNumber, LegacyTemplateDeletedEvent.class, TemplateDeletedEvent.class);
    }

    @Override
    protected TemplateDeletedEvent toDomainEvent(String username) {

        Module.Key moduleKey = new Module.Key(
                this.getModuleName(),
                this.getModuleVersion(),
                null); // TODO Comment récupérer le type de module ?

        return new TemplateDeletedEvent(moduleKey, this.getTemplateName(), new User(username));
    }
}
