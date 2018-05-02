package org.hesperides.domain.technos.entities;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.List;

@Value
public class Techno extends TemplateContainer {

    public static final String NAMESPACE_PREFIX = "packages";

    public Techno(Key key, List<Template> templates) {
        super(key, templates);
    }
}
