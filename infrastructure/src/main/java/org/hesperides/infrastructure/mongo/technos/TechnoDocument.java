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
package org.hesperides.infrastructure.mongo.technos;

import lombok.Data;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Document(collection = "techno")
public class TechnoDocument {
    @Id
    private KeyDocument key;
    private List<TemplateDocument> templates;

    public static TechnoDocument fromDomainInstance(Techno techno) {
        TechnoDocument technoDocument = new TechnoDocument();
        technoDocument.setKey(KeyDocument.fromDomainInstance(techno.getKey()));
        technoDocument.setTemplates(TemplateDocument.fromDomainInstances(techno.getTemplates()));
        return technoDocument;
    }

    public static List<TechnoView> toTechnoViews(List<TechnoDocument> technos) {
        List<TechnoView> technoViews = null;
        if (technos != null) {
            technoViews = technos.stream().map(TechnoDocument::toTechnoView).collect(Collectors.toList());
        }
        return technoViews;
    }

    public TechnoView toTechnoView() {
        return new TechnoView(key.getName(), key.getVersion(), key.isWorkingCopy(),
                TemplateDocument.toTemplateViews(templates, key.toDomainInstance(), Techno.KEY_PREFIX));
    }

    public void addTemplate(TemplateDocument templateDocument) {
        if (templates == null) {
            templates = new ArrayList<>();
        }
        templates.add(templateDocument);
    }
}
