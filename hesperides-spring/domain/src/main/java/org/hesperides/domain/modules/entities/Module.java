package org.hesperides.domain.modules.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.net.URI;
import java.util.List;

/**
 * Entité du domaine
 */
@Value
public class Module {
    @Getter
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key {
        String name;
        String version;
        Type versionType;

        @JsonIgnore
        public URI getURI() {
            return URI.create("/rest/modules/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }

        @Override
        public String toString() {
            return "module-" + name + "-" + version + "-" + versionType.getMinimizedForm();
        }
    }

    /**
     * Type de module possible
     */
    public static enum Type {
        workingcopy("wc"),
        release("release");

        private final String minimizedForm;

        Type(String minimizedForm) {
            this.minimizedForm = minimizedForm;
        }

        public String getMinimizedForm() {
            return minimizedForm;
        }
    }

    Key key;
    List<Techno> technos;
}
