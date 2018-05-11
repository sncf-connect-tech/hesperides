package org.hesperides.infrastructure.mongo.modules;

import lombok.Data;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.technos.TechnoDocument;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
    List<TemplateDocument> templates;
    @DBRef
    List<TechnoDocument> technos;
    Long versionId;

    public static ModuleDocument fromDomain(Module module, List<TechnoDocument> technos) {
        ModuleDocument moduleDocument = new ModuleDocument();
        TemplateContainer.Key key = module.getKey();
        moduleDocument.setName(key.getName());
        moduleDocument.setVersion(key.getVersion());
        moduleDocument.setWorkingCopy(key.getVersionType().equals(TemplateContainer.VersionType.workingcopy));
        moduleDocument.setTemplates(module.getTemplates() != null ? module.getTemplates().stream().map(TemplateDocument::fromDomain).collect(Collectors.toList()) : null);
        moduleDocument.setTechnos(technos);
        moduleDocument.setVersionId(module.getVersionId());
        return moduleDocument;
    }

    public ModuleView toModuleView() {
        TemplateContainer.Key moduleKey = new TemplateContainer.Key(name, version, workingCopy ? TemplateContainer.VersionType.workingcopy : TemplateContainer.VersionType.release);
        return new ModuleView(name, version, workingCopy,
                templates != null ? templates.stream().map(templateDocument -> templateDocument.toTemplateView(moduleKey, Module.KEY_PREFIX)).collect(Collectors.toList()) : null,
                technos != null ? technos.stream().map(TechnoDocument::toTechnoView).collect(Collectors.toList()) : null,
                versionId);
    }
}
