package org.hesperides.infrastructure.mongo.modules;

import lombok.Data;
import org.hesperides.domain.modules.entities.Module;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "module")
@Data
public class ModuleDocument {

    @Id
    String id;

    String name;
    String version;
    Module.Type versionType;
    Long versionId;

}
