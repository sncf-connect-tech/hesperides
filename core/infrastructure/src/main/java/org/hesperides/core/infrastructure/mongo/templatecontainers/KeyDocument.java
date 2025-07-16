/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
package org.hesperides.core.infrastructure.mongo.templatecontainers;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
@NoArgsConstructor
@CompoundIndexes({@CompoundIndex(name = "name_version", def = "{'name' : 1, 'version': 1}")})
public class KeyDocument implements Serializable {

    private String name;
    private String version;
    private boolean isWorkingCopy;

    public KeyDocument(TemplateContainer.Key key) {
        this.name = key.getName();
        this.version = key.getVersion();
        this.isWorkingCopy = key.isWorkingCopy();
    }

    public static TemplateContainerKeyView toKeyView(KeyDocument keyDocument) {
        return new TemplateContainerKeyView(keyDocument.getName(), keyDocument.getVersion(), keyDocument.isWorkingCopy());
    }

    public static List<KeyDocument> fromModelKeys(List<? extends TemplateContainer.Key> keys) {
        return keys.stream()
                .map(KeyDocument::new)
                .collect(Collectors.toList());
    }
}
