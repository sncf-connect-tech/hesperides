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
import org.hesperides.domain.modules.TemplateUpdatedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class LegacyTemplateUpdatedEventTest extends AbstractLegacyCodecTest {

    private static final String JSON_PATH = "templateUpdatedEvent.json";

    /**
     * TODO Faire attention au numéro de séquence généré par Axon par rapport à celui défini dans le legacy
     * Pas sûr qu'on le gère bien
     * Etudier les impacts (dans le legacy notamment)
     */

    @Test
    public void code() throws IOException {
        Module.Key moduleKey = getSampleModuleKey();
        Template template = new Template("foo-template", "foo_template.json", "/destination", "Foo={{foo}}\nBar={{bar}}", new Template.Rights(
                new Template.FileRights(true, false, null),
                new Template.FileRights(true, false, null),
                new Template.FileRights(true, false, null)
        ), moduleKey);
        TemplateUpdatedEvent templateUpdatedEvent = new TemplateUpdatedEvent(moduleKey, template, getSampleUser());

        DomainEventMessage<?> domainEventMessage = new GenericDomainEventMessage("type", "identifier", 2L, templateUpdatedEvent);
        String actualJson = getMockedLegacyCodec().code(domainEventMessage);
        String expectedJson = uglifyJsonLegacyEvent(getResourceContent(JSON_PATH));
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void decode() throws IOException {
        TemplateUpdatedEvent event = getEventFromJson(JSON_PATH, TemplateUpdatedEvent.class);

        assertEquals("foo-war", event.getModuleKey().getName());
        assertEquals("1.0", event.getModuleKey().getVersion());
        assertEquals(Module.Type.workingcopy, event.getModuleKey().getVersionType());

        assertEquals("foo-template", event.getTemplate().getName());
        assertEquals("foo_template.json", event.getTemplate().getFilename());
        assertEquals("/destination", event.getTemplate().getLocation());
        assertEquals("Foo={{foo}}\nBar={{bar}}", event.getTemplate().getContent());

        assertEquals(true, event.getTemplate().getRights().getUser().getRead());
        assertEquals(false, event.getTemplate().getRights().getUser().getWrite());
        assertEquals(null, event.getTemplate().getRights().getUser().getExecute());
        assertEquals(true, event.getTemplate().getRights().getGroup().getRead());
        assertEquals(false, event.getTemplate().getRights().getGroup().getWrite());
        assertEquals(null, event.getTemplate().getRights().getGroup().getExecute());
        assertEquals(null, event.getTemplate().getRights().getOther().getExecute());

        assertEquals("robert", event.getUser().getName());
    }
}
