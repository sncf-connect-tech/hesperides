package org.hesperides.domain.modules.entities;

import lombok.Value;

import java.net.URI;
import java.util.List;

/**
 * Entité du domaine
 */
@Value
public class Module {
    @Value
    public static class Key {
        String name;
        String version;
        Type versionType;

        public URI getURI() {
            return URI.create("/rest/modules/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }

        public String getNamespace() {
            return "modules#" + name + "#" + version + "#" + versionType.name().toUpperCase();
        }

        @Override
        public String toString() {
            return "module-" + name + "-" + version + "-" + versionType.getMinimizedForm();
        }

        public boolean isWorkingCopy() {
            return versionType == Type.workingcopy;
        }
    }

    public enum Type {
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
    Long versionID;

    public Long getVersionID() {
        return versionID != null ? versionID : 1L;
    }

}
