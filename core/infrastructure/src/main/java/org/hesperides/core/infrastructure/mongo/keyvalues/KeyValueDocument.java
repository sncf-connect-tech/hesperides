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
package org.hesperides.core.infrastructure.mongo.keyvalues;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.keyvalues.entities.KeyValue;
import org.hesperides.core.domain.keyvalues.queries.views.KeyValueView;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static org.hesperides.core.infrastructure.mongo.Collections.KEYVALUE;

@Data
@Document(collection = KEYVALUE)
@NoArgsConstructor
public class KeyValueDocument {
    @Id
    private String id;
    private String key;
    private String value;
    private String keyValue;

    public KeyValueDocument(String id, KeyValue keyValue) {
        this.id = id;
        this.key = keyValue.getKey();
        this.value = keyValue.getValue();
        this.keyValue = keyValue.getKeyValue();
    }

    public KeyValueView toKeyValueView() {
        return new KeyValueView(id, key, value, keyValue);
    }
}
