package org.hesperides.domain.technos.entities;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.List;

@Value
public class Techno extends TemplateContainer {

    public static final String KEY_PREFIX = "package";

    public Techno(Key key, List<Template> templates) {
        //TODO Est-ce qu'il faut générer le model ici ?
        super(key, templates, null);
    }
}
