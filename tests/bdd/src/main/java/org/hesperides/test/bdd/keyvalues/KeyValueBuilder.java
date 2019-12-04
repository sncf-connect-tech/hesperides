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
package org.hesperides.test.bdd.keyvalues;

import lombok.Getter;
import org.hesperides.core.presentation.io.keyvalues.KeyValueInput;
import org.hesperides.core.presentation.io.keyvalues.KeyValueOutput;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class KeyValueBuilder implements Serializable {

    @Getter
    String id;
    private String key;
    private String value;

    public KeyValueBuilder() {
        reset();
    }

    public void reset() {
        key = "key";
        value = "value";
    }

    public void withId(String id) {
        this.id = id;
    }

    public void withKey(String key) {
        this.key = key;
    }

    public void withValue(String value) {
        this.value = value;
    }

    public KeyValueInput buildInput() {
        return new KeyValueInput(key, value);
    }

    public KeyValueOutput buildOutput() {
        return new KeyValueOutput(id, key, value, key + value);
    }
}
