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
import org.hesperides.domain.modules.TemplateCreatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.hesperides.domain.security.User;
import org.hesperides.domain.security.UserEvent;

@Value
public class LegacyTemplateCreatedEvent extends AbstractLegacyEvent {
    public static final String EVENT_TYPE = "com.vsct.dt.hesperides.templating.modules.ModuleTemplateCreatedEvent";

    String moduleName;
    String moduleVersion;
    LegacyTemplate created;

    public static String fromDomainEventMessage(DomainEventMessage domainEventMessage) {
        TemplateCreatedEvent domainEvent = (TemplateCreatedEvent) domainEventMessage.getPayload();
        Template template = domainEvent.getTemplate();
        LegacyTemplate legacyTemplate = LegacyTemplate.fromDomainTemplate(template);
        return LEGACY_GSON_SERIALIZER.toJson(new LegacyTemplateCreatedEvent(template.getModuleKey().getName(), template.getModuleKey().getVersion(), legacyTemplate));
    }

    public static DomainEventMessage<? extends UserEvent> toDomainEventMessage(LegacyEvent legacyEvent, String aggregateIdentifier, long sequenceNumber) {
        return legacyEvent.toDomainEventMessage(aggregateIdentifier, sequenceNumber, LegacyTemplateCreatedEvent.class, TemplateCreatedEvent.class);
    }

    @Override
    protected TemplateCreatedEvent toDomainEvent(String username) {
        LegacyTemplate legacyTemplate = this.getCreated();

        Module.Key moduleKey = new Module.Key(
                this.getModuleName(),
                this.getModuleVersion(),
                legacyTemplate.getModuleTypeFromNamespace());

        Template template = legacyTemplate.toDomainTemplate(moduleKey);
        return new TemplateCreatedEvent(moduleKey, template, new User(username));
    }
}
