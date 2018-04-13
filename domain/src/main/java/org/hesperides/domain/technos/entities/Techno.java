package org.hesperides.domain.technos.entities;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class Techno extends TemplateContainer {
    public Techno(Key key, Long versionId) {
        super(key, versionId);
    }
}
