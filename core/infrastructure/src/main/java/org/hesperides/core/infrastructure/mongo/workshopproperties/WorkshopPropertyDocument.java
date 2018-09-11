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
package org.hesperides.core.infrastructure.mongo.workshopproperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.workshopproperties.entities.WorkshopProperty;
import org.hesperides.core.domain.workshopproperties.queries.views.WorkshopPropertyView;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "workshopproperties")
public class WorkshopPropertyDocument {

    @Id
    private String key;
    private String value;
    private String keyValue;

    public WorkshopPropertyDocument(WorkshopProperty workshopProperty) {
        this.key = workshopProperty.getKey();
        this.value = workshopProperty.getValue();
        this.keyValue = workshopProperty.getKeyValue();
    }

    public WorkshopPropertyView toWorkshopPropertyView() {
        return new WorkshopPropertyView(
                key, value, keyValue
        );
    }
}
