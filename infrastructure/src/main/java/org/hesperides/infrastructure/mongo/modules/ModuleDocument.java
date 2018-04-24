package org.hesperides.infrastructure.mongo.modules;

import lombok.Data;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "module")
@Data
public class ModuleDocument {
    @Id
    String id;
    String name;
    String version;
    Module.Type versionType; //TODO Pourquoi pas un booléen
    Long versionId;
    List<TemplateDocument> templates;

    public ModuleView toModuleView() {
        return new ModuleView(name, version, versionType == TemplateContainer.Type.workingcopy, versionId);
    }
}
