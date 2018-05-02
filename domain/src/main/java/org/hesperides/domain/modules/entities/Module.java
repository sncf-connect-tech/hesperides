package org.hesperides.domain.modules.entities;

import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.net.URI;
import java.util.List;

@Value
public class Module extends TemplateContainer {

    public static final String NAMESPACE_PREFIX = "modules";

    List<Techno> technos;
    Long versionId;

    public Module(Key key, List<Template> templates, List<Techno> technos, Long versionId) {
        super(key, templates);
        this.technos = technos;
        this.versionId = versionId;
    }

    public String getNamespace() {
        return getKey().getNamespace(NAMESPACE_PREFIX);
    }

    public URI getURI() {
        return getKey().getURI(NAMESPACE_PREFIX);
    }

    @Override
    public String toString() {
        return getKey().toString("module");
    }
}
