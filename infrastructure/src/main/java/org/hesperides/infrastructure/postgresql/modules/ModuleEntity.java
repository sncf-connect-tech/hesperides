package org.hesperides.infrastructure.postgresql.modules;

import lombok.*;
import org.hesperides.domain.modules.entities.Module;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ModuleEntity {

    @EmbeddedId
    private ModuleEntityId moduleEntityId;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class ModuleEntityId implements Serializable {
        @Column(name = "name")
        String name;

        @Column(name = "version")
        String version;

        @Column(name = "version_type")
        Module.Type versionType;
    }

    @Column(name = "version_id")
    Long versionId;

}
