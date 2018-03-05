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
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public abstract class AbstractLegacyCodecTest {

    /**
     * Récupère une instance de LegacyCodec où les méthodes getContextUsername
     * et getLegacyTimestampFromEventTimestamp sont mockées
     */
    protected LegacyCodec getMockedLegacyCodec() {
        LegacyCodec codec = spy(LegacyCodec.class);
        doReturn(1L).when(codec).getLegacyTimestampFromEventTimestamp(any());
        return codec;
    }

    /**
     * Récupère le contenu d'un fichier à partir de son path
     */
    protected String getResource(final String path) throws IOException {
        return FileUtils.readFileToString(new File(path));
    }

    /**
     * Applatit un json formatté (pretty printed)
     */
    protected String uglifyJsonLegacyEvent(final String prettyJson) {
        Gson gson = new Gson();
        return gson.toJson(gson.fromJson(prettyJson, LegacyEvent.class));
    }
}
