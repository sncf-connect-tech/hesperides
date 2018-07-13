package org.hesperides.core.domain.modules.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.exceptions.OutOfDateVersionException;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;

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

    public Module initializeVersionId() {
        return new Module(getKey(), getTemplates(), technos, 1L);
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
    }
}
