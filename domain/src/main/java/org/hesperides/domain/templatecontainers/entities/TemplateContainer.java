package org.hesperides.domain.templatecontainers.entities;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.net.URI;
import java.util.List;

@Value
@NonFinal
public abstract class TemplateContainer {
    Key key;
    List<Template> templates;

    public TemplateContainer(Key key, List<Template> templates) {
        this.key = key;
        this.templates = templates;
    }

    @Value
    @NonFinal
    public static abstract class Key {
        String name;
        String version;
        VersionType versionType;

        protected abstract String getUriPrefix();

        public URI getURI() {
            return URI.create("/rest" + getUriPrefix() + "/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }

        protected abstract String getNamespacePrefix();

        public String getNamespaceWithPrefix() {
            return getNamespacePrefix() + "#" + getNamespaceWithoutPrefix();
        }

        public String getNamespaceWithoutPrefix() {
            return name + "#" + version + "#" + versionType.name().toUpperCase();
        }

        public boolean isWorkingCopy() {
            return versionType == VersionType.workingcopy;
        }
    }

    public enum VersionType {
        workingcopy("wc"),
        release("release");

        private final String minimizedForm;

        VersionType(String minimizedForm) {
            this.minimizedForm = minimizedForm;
        }

        public String getMinimizedForm() {
            return minimizedForm;
        }

        public static String toString(boolean isWorkingCopy) {
            return isWorkingCopy ? workingcopy.toString() : release.toString();
        }
    }

    public static VersionType getVersionType(boolean isWorkingCopy) {
        return isWorkingCopy ? VersionType.workingcopy : VersionType.release;
    }
}
