package org.hesperides.infrastructure.jpa.modules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.domain.modules.entities.Module;

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

}
