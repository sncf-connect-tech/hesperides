package org.hesperides.core.domain.modules.queries;

import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;

import java.util.List;

@Value
public class ModuleView {
    String name;
    String version;
    boolean isWorkingCopy;
    List<TemplateView> templates;
    List<TechnoView> technos;
    Long versionId;

    public Module.Key getKey() {
        return new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
    }

    public Module toDomainInstance() {
        TemplateContainer.Key moduleKey = getKey();
        return new Module(moduleKey, TemplateView.toDomainInstances(templates, moduleKey), TechnoView.toDomainInstances(technos), versionId);
    }
}
