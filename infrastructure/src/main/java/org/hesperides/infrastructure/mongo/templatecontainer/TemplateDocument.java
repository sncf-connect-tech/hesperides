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
package org.hesperides.infrastructure.mongo.templatecontainer;

import lombok.Data;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class TemplateDocument {
    @Id
    String id;
    String name;
    String filename;
    String location;
    String content;
    //Rights rights;
    Long versionId;

    public static TemplateDocument fromDomain(Template template) {
        TemplateDocument templateDocument = new TemplateDocument();
        templateDocument.setName(template.getName());
        templateDocument.setFilename(template.getFilename());
        templateDocument.setLocation(template.getLocation());
        templateDocument.setContent(template.getContent());
//        templateDocument.setRights(template.getRights()); //TODO
        templateDocument.setVersionId(template.getVersionId());
        return templateDocument;
    }

    public TemplateView toTemplateView() {
        return new TemplateView(
                name,
                "", //TODO
                filename,
                location,
                content,
                null, //todo rights
                versionId
        );
    }
}
