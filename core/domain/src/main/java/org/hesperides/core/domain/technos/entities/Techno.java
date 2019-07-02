package org.hesperides.core.domain.technos.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.KeyView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(callSuper = true)
public class Techno extends TemplateContainer {

    public Techno(TemplateContainer.Key key, List<Template> templates) {
        super(key, templates);
    }

    public static List<String> getTemplatesName(List<Techno> technos) {
        return Optional.ofNullable(technos)
                .orElseGet(Collections::emptyList)
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

        public Key(KeyView key) {
            super(key.getName(), key.getVersion(), TemplateContainer.getVersionType(key.getIsWorkingCopy()));
        }

        public static List<Key> fromViews(List<KeyView> keys) {
            return Optional.ofNullable(keys)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Key::new)
                    .collect(Collectors.toList());
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
