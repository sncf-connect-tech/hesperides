package org.hesperides.domain.modules.entities;

import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.List;

/**
 * Entité du domaine
 */
@Value
public class Module extends TemplateContainer {
    List<Techno> technos;
    public Module (Key key, List<Techno> technos, Long versionId ) {
        super(key, versionId);
        this.technos = technos;
    };
}
