/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-modulelogies/hesperides)
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
package org.hesperides.test.bdd.keyvalues;

import org.hesperides.core.presentation.io.keyvalues.KeyValueInput;
import org.hesperides.core.presentation.io.keyvalues.KeyValueOutput;
import org.hesperides.test.bdd.commons.CustomRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

import static org.hesperides.test.bdd.commons.TestContext.getResponseType;

@Component
public class KeyValueClient {

    private final CustomRestTemplate restTemplate;

    @Autowired
    public KeyValueClient(CustomRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createKeyValue(KeyValueInput keyValueInput) {
        restTemplate.postForEntity("/keyvalues", keyValueInput, String.class);
    }

    public void getKeyValue(URI location) {
        restTemplate.getForEntity(location.toString(), KeyValueOutput.class);
    }

    public void getKeyValue(String id) {
        getKeyValue(id, null);
    }

    public void getKeyValue(String id, String tryTo) {
        restTemplate.getForEntity("/keyvalues/{id}", getResponseType(tryTo, KeyValueOutput.class), id);
    }

    public void updateKeyValue(String id, KeyValueInput keyValueInput) {
        restTemplate.putForEntity("/keyvalues/{id}", keyValueInput, String.class, id);
    }

    public void deleteKeyValue(String id) {
        restTemplate.deleteForEntity("/keyvalues/{id}", String.class, id);
    }
}
