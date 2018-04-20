package org.hesperides.domain.templatecontainer.entities;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.URI;
import java.util.List;

@Value
@NonFinal
public abstract class TemplateContainer {
    Key key;
    List<Template> templates;

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

    @Value
    public static class Key {
        String name;
        String version;
        Type versionType;

        public URI getURI() {
            return URI.create("/rest/modules/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }

        public String getNamespace(String prefix) {
            return prefix + "#" + name + "#" + version + "#" + versionType.name().toUpperCase();
        }

        @Override
        public String toString() {
            return "module-" + name + "-" + version + "-" + versionType.getMinimizedForm();
        }

        public boolean isWorkingCopy() {
            return versionType == Type.workingcopy;
        }
    }
}
