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

    @Value
    public static class Key {
        String name;
        String version;
        VersionType versionType;

        public URI getURI(String prefix) {
            return URI.create("/rest/" + prefix + "s/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }

        public String getNamespace(String prefix) {
            return prefix + "s#" + name + "#" + version + "#" + versionType.name().toUpperCase();
        }

        public String toString(String prefix) {
            return prefix + "-" + name + "-" + version + "-" + versionType.getMinimizedForm();
        }

        public boolean isWorkingCopy() {
            return versionType == VersionType.workingcopy;
        }
    }

    public static VersionType getVersionType(boolean isWorkingCopy) {
        return isWorkingCopy ? VersionType.workingcopy : VersionType.release;
    }
}
