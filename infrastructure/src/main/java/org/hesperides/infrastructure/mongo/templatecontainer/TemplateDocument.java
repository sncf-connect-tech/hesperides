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
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class TemplateDocument {
    String name;
    String filename;
    String location;
    String content;
    RightsDocument rights;
    Long versionId;

    public static TemplateDocument fromDomain(Template template) {
        TemplateDocument templateDocument = new TemplateDocument();
        templateDocument.setName(template.getName());
        templateDocument.setFilename(template.getFilename());
        templateDocument.setLocation(template.getLocation());
        templateDocument.setContent(template.getContent());
        templateDocument.setRights(RightsDocument.fromDomain(template.getRights()));
        templateDocument.setVersionId(template.getVersionId());
        return templateDocument;
    }

    public TemplateView toTemplateView(TemplateContainer.Key key, String namespacePrefix) {
        return new TemplateView(
                name,
                key.getNamespace(namespacePrefix),
                filename,
                location,
                content,
                rights.toRightsView(),
                versionId
        );
    }

    @Data
    public static class RightsDocument {
        FileRightsView user;
        FileRightsView group;
        FileRightsView other;

        public static RightsDocument fromDomain(Template.Rights rights) {
            RightsDocument result = null;
            if (rights != null) {
                result = new RightsDocument();
                result.setUser(FileRightsView.fromDomain(rights.getUser()));
                result.setGroup(FileRightsView.fromDomain(rights.getGroup()));
                result.setOther(FileRightsView.fromDomain(rights.getOther()));
            }
            return result;
        }

        public TemplateView.RightsView toRightsView() {
            TemplateView.FileRightsView userRights = user != null ? user.toFileRightsView() : null;
            TemplateView.FileRightsView groupRights = group != null ? group.toFileRightsView() : null;
            TemplateView.FileRightsView otherRights = other != null ? other.toFileRightsView() : null;
            return new TemplateView.RightsView(userRights, groupRights, otherRights);
        }
    }

    @Data
    public static class FileRightsView {
        Boolean read;
        Boolean write;
        Boolean execute;

        public static FileRightsView fromDomain(Template.FileRights fileRights) {
            FileRightsView result = null;
            if (fileRights != null) {
                result = new FileRightsView();
                result.setRead(fileRights.getRead());
                result.setWrite(fileRights.getWrite());
                result.setExecute(fileRights.getExecute());
            }
            return result;
        }

        public TemplateView.FileRightsView toFileRightsView() {
            return new TemplateView.FileRightsView(read, write, execute);
        }
    }
}
