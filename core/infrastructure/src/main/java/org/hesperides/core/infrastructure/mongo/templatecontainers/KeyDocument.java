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
package org.hesperides.core.infrastructure.mongo.templatecontainers;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document
@NoArgsConstructor
public class KeyDocument implements Serializable {

    private String name;
    private String version;
    private boolean isWorkingCopy;

    public KeyDocument(TemplateContainer.Key key) {
        this.name = key.getName();
        this.version = key.getVersion();
        this.isWorkingCopy = key.isWorkingCopy();
    }
}
