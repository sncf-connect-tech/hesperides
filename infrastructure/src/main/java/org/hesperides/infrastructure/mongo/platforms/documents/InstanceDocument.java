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
import org.hesperides.domain.platforms.entities.Instance;
import org.hesperides.domain.platforms.queries.views.InstanceView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class InstanceDocument {

    private String name;
    List<ValorisedPropertyDocument> valorisedProperties;

    public static List<InstanceDocument> fromDomainInstances(List<Instance> instances) {
        List<InstanceDocument> instanceDocuments = null;
        if (instances != null) {
            instanceDocuments = instances.stream().map(InstanceDocument::fromDomainInstance).collect(Collectors.toList());
        }
        return instanceDocuments;
    }

    public static InstanceDocument fromDomainInstance(Instance instance) {
        InstanceDocument instanceDocument = new InstanceDocument();
        instanceDocument.setName(instance.getName());
        instanceDocument.setValorisedProperties(ValorisedPropertyDocument.fromDomainInstances(instance.getValorisedProperties()));
        return instanceDocument;
    }

    public static List<InstanceView> toInstanceViews(List<InstanceDocument> instances) {
        List<InstanceView> instanceViews = null;
        if (instances != null) {
            instanceViews = instances.stream().map(InstanceDocument::toInstanceView).collect(Collectors.toList());
        }
        return instanceViews;
    }

    public InstanceView toInstanceView() {
        return new InstanceView(
                name,
                ValorisedPropertyDocument.toValorisedPropertyViews(valorisedProperties)
        );
    }
}
