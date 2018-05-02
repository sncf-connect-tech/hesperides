package org.hesperides.domain.technos.entities;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.net.URI;
import java.util.List;

@Value
public class Techno extends TemplateContainer {

    public static final String NAMESPACE_PREFIX = "technos";

    public Techno(Key key, List<Template> templates) {
        super(key, templates);
    }

    public String getNamespace() {
        return getKey().getNamespace(NAMESPACE_PREFIX);
    }

    public URI getURI() {
        return getKey().getURI(NAMESPACE_PREFIX);
    }

    @Override
    public String toString() {
        return getKey().toString("techno");
    }
}
