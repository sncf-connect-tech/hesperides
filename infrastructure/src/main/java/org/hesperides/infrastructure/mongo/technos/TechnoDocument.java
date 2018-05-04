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
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "techno")
@Data
public class TechnoDocument {
    @Id
    String id;
    String name;
    String version;
    boolean workingCopy;
    List<TemplateDocument> templates;

    public static TechnoDocument fromDomain(Techno techno) {
        TechnoDocument technoDocument = new TechnoDocument();
        TemplateContainer.Key key = techno.getKey();
        technoDocument.setName(key.getName());
        technoDocument.setVersion(key.getVersion());
        technoDocument.setWorkingCopy(key.isWorkingCopy());
        technoDocument.setTemplates(techno.getTemplates() != null ? techno.getTemplates().stream().map(TemplateDocument::fromDomain).collect(Collectors.toList()) : null);
        return technoDocument;
    }

    public TechnoView toTechnoView() {
        TemplateContainer.Key technoKey = new TemplateContainer.Key(name, version, workingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
        return new TechnoView(name, version, workingCopy,
                templates != null ? templates.stream().map(templateDocument -> templateDocument.toTemplateView(technoKey, Techno.KEY_PREFIX)).collect(Collectors.toList()) : null);
    }
}
