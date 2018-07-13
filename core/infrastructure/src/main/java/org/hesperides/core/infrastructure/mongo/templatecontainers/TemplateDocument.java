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
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Document
@NoArgsConstructor
public class TemplateDocument {

    private String name;
    private String filename;
    private String location;
    private String content;
    private RightsDocument rights;
    private Long versionId;

    public TemplateDocument(Template template) {
        this.name = template.getName();
        this.filename = template.getFilename();
        this.location = template.getLocation();
        this.content = template.getContent();
        this.rights = RightsDocument.fromDomainInstance(template.getRights());
        this.versionId = template.getVersionId();
    }

    public static List<TemplateDocument> fromDomainInstances(List<Template> templates) {
        return Optional.ofNullable(templates)
                .orElse(Collections.emptyList())
                .stream()
                .map(TemplateDocument::new)
                .collect(Collectors.toList());
    }

    public static List<TemplateView> toTemplateViews(List<TemplateDocument> templateDocuments, TemplateContainer.Key key) {
        return Optional.ofNullable(templateDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(templateDocument -> templateDocument.toTemplateView(key))
                .collect(Collectors.toList());
    }

    public static List<Template> toDomainInstances(List<TemplateDocument> templateDocuments, TemplateContainer.Key key) {
        return Optional.ofNullable(templateDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(templateDocument -> templateDocument.toDomainInstance(key))
                .collect(Collectors.toList());
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
