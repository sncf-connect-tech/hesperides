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

import java.util.List;
import java.util.stream.Collectors;

@Data
@Document
public class TemplateDocument {
    private String name;
    private String filename;
    private String location;
    private String content;
    private RightsDocument rights;
    private Long versionId;

    public static TemplateDocument fromDomainInstance(Template template) {
        TemplateDocument templateDocument = new TemplateDocument();
        templateDocument.setName(template.getName());
        templateDocument.setFilename(template.getFilename());
        templateDocument.setLocation(template.getLocation());
        templateDocument.setContent(template.getContent());
        templateDocument.setRights(RightsDocument.fromDomainInstance(template.getRights()));
        templateDocument.setVersionId(template.getVersionId());
        return templateDocument;
    }

    public static List<TemplateDocument> fromDomainInstances(List<Template> templates) {
        List<TemplateDocument> templateDocuments = null;
        if (templates != null) {
            templateDocuments = templates.stream().map(TemplateDocument::fromDomainInstance).collect(Collectors.toList());
        }
        return templateDocuments;
    }

    public static List<TemplateView> toTemplateViews(List<TemplateDocument> templates, TemplateContainer.Key key, String keyPrefix) {
        List<TemplateView> templateViews = null;
        if (templates != null) {
            templateViews = templates.stream().map(templateDocument -> templateDocument.toTemplateView(key, keyPrefix)).collect(Collectors.toList());
        }
        return templateViews;
    }

    public TemplateView toTemplateView(TemplateContainer.Key key, String namespacePrefix) {
        return new TemplateView(
                name,
                key.getNamespace(namespacePrefix),
                filename,
                location,
                content,
                RightsDocument.toRightsView(rights),
                versionId
        );
    }

    @Data
    public static class RightsDocument {
        private FileRightsDocument user;
        private FileRightsDocument group;
        private FileRightsDocument other;

        public static RightsDocument fromDomainInstance(Template.Rights rights) {
            RightsDocument rightsDocument = null;
            if (rights != null) {
                rightsDocument = new RightsDocument();
                rightsDocument.setUser(FileRightsDocument.fromDomainInstance(rights.getUser()));
                rightsDocument.setGroup(FileRightsDocument.fromDomainInstance(rights.getGroup()));
                rightsDocument.setOther(FileRightsDocument.fromDomainInstance(rights.getOther()));
            }
            return rightsDocument;
        }

        public static TemplateView.RightsView toRightsView(RightsDocument rightsDocument) {
            TemplateView.RightsView rightsView = null;
            if (rightsDocument != null) {
                rightsView = new TemplateView.RightsView(
                        FileRightsDocument.toFileRightsView(rightsDocument.getUser()),
                        FileRightsDocument.toFileRightsView(rightsDocument.getGroup()),
                        FileRightsDocument.toFileRightsView(rightsDocument.getOther()));
            }
            return rightsView;
        }
    }

    @Data
    public static class FileRightsDocument {
        private Boolean read;
        private Boolean write;
        private Boolean execute;

        public static FileRightsDocument fromDomainInstance(Template.FileRights fileRights) {
            FileRightsDocument fileRightsDocument = null;
            if (fileRights != null) {
                fileRightsDocument = new FileRightsDocument();
                fileRightsDocument.setRead(fileRights.getRead());
                fileRightsDocument.setWrite(fileRights.getWrite());
                fileRightsDocument.setExecute(fileRights.getExecute());
            }
            return fileRightsDocument;
        }

        public static TemplateView.FileRightsView toFileRightsView(FileRightsDocument fileRightsDocument) {
            TemplateView.FileRightsView fileRightsView = null;
            if (fileRightsDocument != null) {
                fileRightsView = new TemplateView.FileRightsView(fileRightsDocument.getRead(), fileRightsDocument.getWrite(), fileRightsDocument.getExecute());
            }
            return fileRightsView;
        }
    }
}
