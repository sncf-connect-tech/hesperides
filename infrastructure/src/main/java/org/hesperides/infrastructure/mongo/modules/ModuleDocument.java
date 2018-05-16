package org.hesperides.infrastructure.mongo.modules;

import lombok.Data;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Document(collection = "module")
public class ModuleDocument {
    @Id
    private KeyDocument key;
    private List<TemplateDocument> templates;
    @DBRef
    private List<TechnoDocument> technos;
    private Long versionId;

    public static ModuleDocument fromDomainInstance(Module module, List<TechnoDocument> technoDocuments) {
        ModuleDocument moduleDocument = new ModuleDocument();
        moduleDocument.setKey(KeyDocument.fromDomainInstance(module.getKey()));
        moduleDocument.setTemplates(TemplateDocument.fromDomainInstances(module.getTemplates()));
        moduleDocument.setTechnos(technoDocuments);
        moduleDocument.setVersionId(module.getVersionId());
        return moduleDocument;
    }

    public ModuleView toModuleView() {
        return new ModuleView(key.getName(), key.getVersion(), key.isWorkingCopy(),
                TemplateDocument.toTemplateViews(templates, key.toDomainInstance(), Module.KEY_PREFIX),
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
        // Solution A
        removeTemplate(updatedTemplateDocument.getName());
        addTemplate(updatedTemplateDocument);

        // Solution B
        /*setTemplates(templates.stream()
                .map(existingTemplateDocument -> existingTemplateDocument.getName().equalsIgnoreCase(updatedTemplateDocument.getName()) ? updatedTemplateDocument : existingTemplateDocument)
                .collect(Collectors.toList()));*/

        // Solution C
        /*for (int i = 0; i < templates.size(); i++) {
            if (templates.get(i).getName().equalsIgnoreCase(updatedTemplateDocument.getName())) {
                templates.set(i, updatedTemplateDocument);
                break;
            }
        }*/
    }

    public void removeTemplate(String templateName) {
        templates.removeIf(templateDocument -> templateDocument.getName().equalsIgnoreCase(templateName));
    }

    public Optional<TemplateDocument> findOptionalTemplateByName(String templateName) {
        return templates.stream().filter(templateDocument -> templateDocument.getName().equalsIgnoreCase(templateName)).findFirst();
    }
}
