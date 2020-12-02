package org.hesperides.core.infrastructure.mongo.modules;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.queries.ModulePasswordProperties;
import org.hesperides.core.domain.modules.queries.ModulePropertiesView;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.templatecontainers.entities.AbstractProperty;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.PropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.hesperides.core.infrastructure.mongo.Collections.MODULE;

@Data
@Document(collection = MODULE)
@NoArgsConstructor
public class ModuleDocument {

    @Id
    private String id;
    private KeyDocument key;
    private List<TemplateDocument> templates;
    @DBRef
    private List<TechnoDocument> technos;
    private List<AbstractPropertyDocument> properties;
    private Long versionId;

    public ModuleDocument(String id, Module module, List<TechnoDocument> technoDocuments) {
        this.id = id;
        this.key = new KeyDocument(module.getKey());
        this.templates = TemplateDocument.fromDomainInstances(module.getTemplates());
        this.technos = technoDocuments;
        this.versionId = module.getVersionId();
    }

    public ModuleView toModuleView() {
        TemplateContainer.Key moduleKey = getDomainKey();
        return new ModuleView(key.getName(), key.getVersion(), key.isWorkingCopy(),
                TemplateDocument.toTemplateViews(templates, moduleKey),
                TechnoDocument.toTechnoViews(technos),
                versionId);
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

    void removeTemplate(String templateName) {
        templates.removeIf(templateDocument -> templateDocument.getName().equalsIgnoreCase(templateName));
    }

    public void extractPropertiesAndSave(MongoModuleRepository moduleRepository) {
        this.setProperties(extractPropertiesFromTemplatesAndTechnos());
        moduleRepository.save(this);
    }

    private List<AbstractPropertyDocument> extractPropertiesFromTemplatesAndTechnos() {
        List<Template> allTemplates = getDomainTemplatesFromTemplateDocumentsAndTechnoDocuments();
        List<AbstractProperty> abstractProperties = AbstractProperty.extractPropertiesFromTemplates(allTemplates, key.toString());
        return AbstractPropertyDocument.fromDomainInstances(abstractProperties);
    }

    private List<Template> getDomainTemplatesFromTemplateDocumentsAndTechnoDocuments() {
        Module module = this.toDomainInstance();
        List<Template> allTemplates = new ArrayList<>();
        if (module.getTemplates() != null) {
            allTemplates.addAll(module.getTemplates());
        }
        if (module.getTechnos() != null) {
            module.getTechnos().forEach(techno -> {
                if (techno.getTemplates() != null) {
                    allTemplates.addAll(techno.getTemplates());
                }
            });
        }
        return allTemplates;
    }

    public Module toDomainInstance() {
        TemplateContainer.Key moduleKey = getDomainKey();
        return new Module(
                moduleKey,
                TemplateDocument.toDomainInstances(templates, moduleKey),
                TechnoDocument.toDomainInstances(technos),
                versionId
        );
    }

    public Module.Key getDomainKey() {
        return new Module.Key(key.getName(), key.getVersion(), TemplateContainer.getVersionType(key.isWorkingCopy()));
    }

    public ModulePropertiesView toModulePropertiesView() {
        return new ModulePropertiesView(
                getDomainKey(),
                properties.stream()
                        .map(AbstractPropertyDocument::toView)
                        .collect(Collectors.toList()));
    }

    public ModulePasswordProperties toModulePasswordProperties() {
        Set<String> passwords = Optional.ofNullable(properties)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(PropertyDocument.class::isInstance)
                .map(PropertyDocument.class::cast)
                .filter(PropertyDocument::isPassword)
                .map(PropertyDocument::getName)
                .collect(toSet());

        return new ModulePasswordProperties(getDomainKey(), passwords);
    }
}
