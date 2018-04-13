package org.hesperides.domain.templatecontainer.entities;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.URI;
@Value
@NonFinal
public abstract class TemplateContainer {
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
    Long versionId;
}
