package org.hesperides.core.domain.templatecontainers.queries;

import lombok.Value;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

@Value
public class TemplateContainerKeyView {

    String name;
    String version;
    Boolean isWorkingCopy;

    @Override
    public String toString() {
        return name + "/" + version + "/" + TemplateContainer.getVersionType(isWorkingCopy);
    }

    public Techno.Key toTechnoKey() {
        return new Techno.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
    }
}
