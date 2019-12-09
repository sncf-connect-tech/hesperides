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
package org.hesperides.core.application.keyvalues;

import org.hesperides.core.domain.keyvalues.commands.KeyValueCommands;
import org.hesperides.core.domain.keyvalues.entities.KeyValue;
import org.hesperides.core.domain.keyvalues.queries.KeyValueQueries;
import org.hesperides.core.domain.keyvalues.queries.views.KeyValueView;
import org.hesperides.core.domain.security.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyValueUseCases {

    private final KeyValueCommands keyValueCommands;
    private final KeyValueQueries keyValueQueries;

    @Autowired
    public KeyValueUseCases(KeyValueCommands keyValueCommands, KeyValueQueries keyValueQueries) {
        this.keyValueCommands = keyValueCommands;
        this.keyValueQueries = keyValueQueries;
    }

    public String createKeyValue(KeyValue keyValue, User user) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public KeyValueView getKeyValue(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void updateKeyValue(String id, KeyValue keyValue, User user) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
