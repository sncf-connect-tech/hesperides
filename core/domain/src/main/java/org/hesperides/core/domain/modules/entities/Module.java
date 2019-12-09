package org.hesperides.core.domain.modules.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.modules.exceptions.UpdateReleaseException;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class Module extends TemplateContainer {

    List<Techno> technos;
    Long versionId;

    public Module(TemplateContainer.Key key, List<Template> templates, List<Techno> technos, Long versionId) {
        super(key, templates);
        this.technos = technos;
        this.versionId = versionId;
    }

    @Override
    public Module validateTemplates() {
        return (Module) super.validateTemplates();
    }

    public Module initializeVersionId() {
        return new Module(getKey(), getTemplates(), technos, 1L);
    }

    public Module validateIsWorkingCopy() {
        if (!getKey().isWorkingCopy()) {
            throw new UpdateReleaseException(getKey());
        }
        return this;
    }

    public Module validateVersionId(Long expectedVersionId) {
        if (!expectedVersionId.equals(versionId)) {
            throw new OutOfDateVersionException(expectedVersionId, versionId);
        }
        return this;
    }

    public Module incrementVersiondId() {
        return new Module(
                getKey(),
                getTemplates(),
                technos,
                versionId + 1
        );
    }

    /**
     * Liste les noms des templates du module et des technos du module.
     */
    public List<String> getTemplatesName() {
        return Stream.concat(
                super.getTemplatesName().stream(),
                Techno.getTemplatesName(technos).stream())
                .collect(Collectors.toList());
    }

    public static class Key extends TemplateContainer.Key {

        private static final String URI_PREFIX = "/modules";
        private static final String NAMESPACE_PREFIX = "modules";
        private static final String TOSTRING_PREFIX = "module";

        public Key(String name, String version, VersionType versionType) {
            super(name, version, versionType);
        }

        @Override
        protected String getUriPrefix() {
            return URI_PREFIX;
        }

        @Override
        protected String getNamespacePrefix() {
            return NAMESPACE_PREFIX;
        }

        public String toString() {
            return TOSTRING_PREFIX + super.toString();
        }

        public static Key fromPropertiesPath(final String propertiesPath) {
            String[] parts = propertiesPath.split("#");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Too short properties path: " + propertiesPath);
            }
            VersionType versionType = VersionType.fromName(parts[parts.length - 1]);
            return new Key(parts[parts.length - 3], parts[parts.length - 2], versionType);
        }

        public static Optional<Key> fromSearchInput(String input) {
            Key result = null;
            String[] values = input.split(" ");
            String name = values.length > 0 ? values[0] : "";
            String version = values.length > 1 ? values[1] : "";
            String workingCopy = values.length > 2 ? values[2] : "";
            if (values.length > 1) {
                boolean isWorkingCopy = !"false".equalsIgnoreCase(workingCopy);
                result = new Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
            }
            return Optional.ofNullable(result);
        }
    }
}
