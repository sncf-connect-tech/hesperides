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
package org.hesperides.infrastructure.mongo.platforms.documents;

import lombok.Data;
import org.hesperides.domain.platforms.entities.Platform;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document
public class PlatformKeyDocument implements Serializable {

    private String applicationName;
    private String platformName;
    private String version;

    public static PlatformKeyDocument fromDomainInstance(Platform.Key platformKey) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument();
        platformKeyDocument.setApplicationName(platformKey.getApplicationName());
        platformKeyDocument.setPlatformName(platformKey.getPlatformName());
        platformKeyDocument.setVersion(platformKey.getVersion());
        return platformKeyDocument;
    }
}
