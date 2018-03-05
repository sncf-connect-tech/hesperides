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

import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.hesperides.domain.modules.ModuleCreatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LegacyModuleCreatedEventTest extends AbstractLegacyCodecTest {

    private static final String JSON_PATH = "moduleCreatedEvent.json";

    @Test
    public void code() throws IOException {
        ModuleCreatedEvent moduleCreatedEvent = new ModuleCreatedEvent(new Module(new Module.Key("foo-war", "1.0", Module.Type.workingcopy), new ArrayList<>(), 1L));
        DomainEventMessage<?> domainEventMessage = new GenericDomainEventMessage("type", "identifier", 0L, moduleCreatedEvent);
        String actualJson = getMockedLegacyCodec().code(domainEventMessage);
        String expectedJson = uglifyJsonLegacyEvent(getResourceContent(JSON_PATH));
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void decode() throws IOException {
        String inputJson = getResourceContent(JSON_PATH);
        List<DomainEventMessage<?>> list = new LegacyCodec().decode("id", 0, Arrays.asList(inputJson));
        DomainEventMessage<ModuleCreatedEvent> domainEventMessage = (DomainEventMessage<ModuleCreatedEvent>) list.get(0);

        assertEquals("id", domainEventMessage.getAggregateIdentifier());
        assertEquals(0, domainEventMessage.getSequenceNumber());
        assertEquals(ModuleCreatedEvent.class.getName(), domainEventMessage.getPayloadType().getName());

        ModuleCreatedEvent event = domainEventMessage.getPayload();
        Module.Key moduleKey = event.getModule().getKey();

        assertEquals("foo-war", moduleKey.getName());
        assertEquals("1.0", moduleKey.getVersion());
        assertEquals(Module.Type.workingcopy, moduleKey.getVersionType());
    }
}
