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
package org.hesperides.infrastructure.mongo.templatecontainers;

import lombok.Data;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
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

    public static List<TemplateView> toTemplateViews(List<TemplateDocument> templates, TemplateContainer.Key key) {
        List<TemplateView> templateViews = null;
        if (templates != null) {
            templateViews = templates.stream().map(templateDocument -> templateDocument.toTemplateView(key)).collect(Collectors.toList());
        }
        return templateViews;
    }

    public static List<Template> toDomainInstances(List<TemplateDocument> templateDocuments, TemplateContainer.Key key) {
        List<Template> templates = null;
        if (templateDocuments != null) {
            templates = templateDocuments.stream().map(templateDocument -> templateDocument.toDomainInstance(key)).collect(Collectors.toList());
        }
        return templates;
    }

    private Template toDomainInstance(TemplateContainer.Key key) {
        return new Template(
                name,
                filename,
                location,
                content,
                RightsDocument.toDomainInstance(rights),
                versionId,
                key
        );
    }

    public TemplateView toTemplateView(TemplateContainer.Key key) {
        return new TemplateView(
                name,
                key.getNamespaceWithPrefix(),
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
                        FileRightsDocument.toFileRightsView(rightsDocument.user),
                        FileRightsDocument.toFileRightsView(rightsDocument.group),
                        FileRightsDocument.toFileRightsView(rightsDocument.other));
            }
            return rightsView;
        }

        public static Template.Rights toDomainInstance(RightsDocument rightsDocument) {
            Template.Rights rights = null;
            if (rightsDocument != null) {
                rights = new Template.Rights(
                        FileRightsDocument.toDomainInstance(rightsDocument.user),
                        FileRightsDocument.toDomainInstance(rightsDocument.group),
                        FileRightsDocument.toDomainInstance(rightsDocument.other)
                );
            }
            return rights;
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
                fileRightsView = new TemplateView.FileRightsView(
                        fileRightsDocument.read,
                        fileRightsDocument.write,
                        fileRightsDocument.execute
                );
            }
            return fileRightsView;
        }

        public static Template.FileRights toDomainInstance(FileRightsDocument fileRightsDocument) {
            Template.FileRights fileRights = null;
            if (fileRightsDocument != null) {
                fileRights = new Template.FileRights(
                        fileRightsDocument.read,
                        fileRightsDocument.write,
                        fileRightsDocument.execute
                );
            }
            return fileRights;
        }
    }
}
