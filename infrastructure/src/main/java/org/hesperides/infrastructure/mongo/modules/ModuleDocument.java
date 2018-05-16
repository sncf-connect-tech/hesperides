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

import java.util.List;

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
}
