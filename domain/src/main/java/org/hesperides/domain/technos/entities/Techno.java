package org.hesperides.domain.technos.entities;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.List;

@Value
public class Techno extends TemplateContainer {

    public Techno(TemplateContainer.Key key, List<Template> templates) {
        //TODO Est-ce qu'il faut générer le model ici ?
        super(key, templates, null);
    }

    public static class Key extends TemplateContainer.Key {

        private static final String PREFIX = "package";

        public Key(String name, String version, VersionType versionType) {
            super(name, version, versionType);
        }

        @Override
        protected String getPrefix() {
            return PREFIX;
        }
    }
}
