package org.hesperides.infrastructure.mongo.modules;

import lombok.Data;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "module")
@Data
public class ModuleDocument {
    @Id
    String id;
    String name;
    String version;
    boolean workingCopy;
    Long versionId;
    List<TemplateDocument> templates;
    //TODO Technos

    public static ModuleDocument fromDomain(Module module) {
        ModuleDocument moduleDocument = new ModuleDocument();
        TemplateContainer.Key key = module.getKey();
        moduleDocument.setName(key.getName());
        moduleDocument.setVersion(key.getVersion());
        moduleDocument.setWorkingCopy(key.getVersionType().equals(TemplateContainer.Type.workingcopy));
        moduleDocument.setVersionId(module.getVersionId());
        moduleDocument.setTemplates(module.getTemplates() != null ? module.getTemplates().stream().map(TemplateDocument::fromDomain).collect(Collectors.toList()) : null);
        //TODO Technos
        return moduleDocument;
    }

    public ModuleView toModuleView() {
        TemplateContainer.Key moduleKey = new TemplateContainer.Key(name, version, workingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
        return new ModuleView(name, version, workingCopy, versionId,
                templates != null ? templates.stream().map(templateDocument -> templateDocument.toTemplateView(moduleKey, Module.NAMESPACE_PREFIX)).collect(Collectors.toList()) : null,
                null); //TODO Technos
    }
}
