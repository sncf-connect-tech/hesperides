package org.hesperides.infrastructure.elasticsearch.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hesperides.domain.modules.entities.Module;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "hesperides", type = "modules")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ModuleDocument {
    @Id
    String id;

    String name;
    String version;
    Module.Type versionType;
    Long versionId;
}
