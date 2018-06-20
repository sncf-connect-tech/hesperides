package org.hesperides.infrastructure.jpa.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity(name = "module")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModuleEntity {
    @EmbeddedId
    private ModuleEntityId moduleEntityId;
    Long versionId;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class ModuleEntityId implements Serializable {
        String name;
        String version;
        TemplateContainer.VersionType versionType;
    }

    public ModuleView toModuleView() {
        return new ModuleView(moduleEntityId.name, moduleEntityId.version, moduleEntityId.versionType.equals(TemplateContainer.VersionType.workingcopy), null, null, versionId);
    }
}
