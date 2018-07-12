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
package org.hesperides.core.infrastructure.mongo.technos;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Document(collection = "techno")
@NoArgsConstructor
public class TechnoDocument {
    @Id
    private KeyDocument key;
    private List<TemplateDocument> templates;
    private List<AbstractPropertyDocument> properties;

    public TechnoDocument(Techno techno) {
        this.key = new KeyDocument(techno.getKey());
        this.templates = TemplateDocument.fromDomainInstances(techno.getTemplates());
    }

    public static List<TechnoView> toTechnoViews(List<TechnoDocument> technos) {
        return Optional.ofNullable(technos)
                .orElse(Collections.emptyList())
                .stream()
                .map(TechnoDocument::toTechnoView)
                .collect(Collectors.toList());
    }

    public static List<Techno> toDomainInstances(List<TechnoDocument> technoDocuments) {
        return Optional.ofNullable(technoDocuments)
                .orElse(Collections.emptyList())
                .stream()
                .map(TechnoDocument::toDomainInstance)
                .collect(Collectors.toList());
    }

    public Techno toDomainInstance() {
        TemplateContainer.Key technoKey = getDomainKey();
        return new Techno(
                technoKey,
                TemplateDocument.toDomainInstances(templates, technoKey)
        );
    }

    public TechnoView toTechnoView() {
        TemplateContainer.Key technoKey = getDomainKey();
        return new TechnoView(key.getName(), key.getVersion(), key.isWorkingCopy(),
                TemplateDocument.toTemplateViews(templates, technoKey));
    }

    public void addTemplate(TemplateDocument templateDocument) {
        if (templates == null) {
            templates = new ArrayList<>();
        }
        templates.add(templateDocument);
    }

    public void updateTemplate(TemplateDocument updatedTemplateDocument) {
        removeTemplate(updatedTemplateDocument.getName());
        addTemplate(updatedTemplateDocument);
    }

    public void removeTemplate(String templateName) {
        templates.removeIf(templateDocument -> templateDocument.getName().equalsIgnoreCase(templateName));
    }

    public void extractPropertiesAndSave(MongoTechnoRepository technoRepository) {
        this.setProperties(extractPropertiesFromTemplates());
        technoRepository.save(this);
    }

    private List<AbstractPropertyDocument> extractPropertiesFromTemplates() {
        TemplateContainer.Key technoKey = getDomainKey();
        List<Template> templates = TemplateDocument.toDomainInstances(this.templates, technoKey);
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplates(templates);
        List<AbstractPropertyDocument> abstractPropertyDocuments = AbstractPropertyDocument.fromDomainInstances(abstractProperties);
        return abstractPropertyDocuments;
    }

    private TemplateContainer.Key getDomainKey() {
        return new Techno.Key(key.getName(), key.getVersion(), TemplateContainer.getVersionType(key.isWorkingCopy()));
    }
}
