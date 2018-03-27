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

import org.hesperides.domain.modules.TemplateDeletedEvent;
import org.hesperides.domain.modules.entities.Module;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class LegacyTemplateDeletedEventTest extends AbstractLegacyCodecTest {

    private static final String JSON_PATH = "templateDeletedEvent.json";

    @Test
    public void code() throws IOException {
        Module.Key key = getSampleModuleKey();
        TemplateDeletedEvent templateDeletedEvent = new TemplateDeletedEvent(key, "foo-template", getSampleUser());
        assertEventEncoding(templateDeletedEvent, JSON_PATH);
    }

    @Test
    public void decode() throws IOException {
        TemplateDeletedEvent event = getEventFromJson(JSON_PATH, TemplateDeletedEvent.class);

        assertEquals("foo-war", event.getModuleKey().getName());
        assertEquals("1.0", event.getModuleKey().getVersion());
        assertEquals(null, event.getModuleKey().getVersionType());
        assertEquals("foo-template", event.getTemplateName());
        assertEquals("robert", event.getUser().getName());
    }
}
