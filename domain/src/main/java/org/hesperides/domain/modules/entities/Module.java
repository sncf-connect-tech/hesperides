package org.hesperides.domain.modules.entities;

import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.List;

@Value
public class Module extends TemplateContainer {

    List<Techno> technos;
    Long versionId;

    public Module(TemplateContainer.Key key, List<Template> templates, List<Techno> technos, Long versionId) {
        //TODO Est-ce qu'il faut générer le model ici ?
        super(key, templates, null);
        this.technos = technos;
        this.versionId = versionId;
    }

    public static class Key extends TemplateContainer.Key {

        public Key(String name, String version, VersionType versionType) {
            super(name, version, versionType);
        }

        @Override
        protected String getPrefix() {
            return "module";
        }
    }
}
