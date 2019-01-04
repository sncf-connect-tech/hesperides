package org.hesperides.core.domain.technos.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@EqualsAndHashCode(callSuper = true)
public class Techno extends TemplateContainer {

    public Techno(TemplateContainer.Key key, List<Template> templates) {
        super(key, templates);
    }

    public static List<String> getTemplatesName(List<Techno> technos) {
        return Optional.ofNullable(technos)
                .orElse(Collections.emptyList())
                .stream()
                .map(TemplateContainer::getTemplatesName)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public static class Key extends TemplateContainer.Key {

        private static final String URI_PREFIX = "/templates/packages";
        private static final String NAMESPACE_PREFIX = "packages";
        private static final String TOSTRING_PREFIX = "techno";

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
