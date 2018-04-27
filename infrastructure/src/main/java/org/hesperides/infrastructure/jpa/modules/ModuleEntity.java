package org.hesperides.infrastructure.jpa.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

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
        Module.Type versionType;
    }

    public ModuleView toModuleView() {
        return new ModuleView(moduleEntityId.name, moduleEntityId.version, moduleEntityId.versionType.equals(TemplateContainer.Type.workingcopy), versionId, null, null);
    }
}
