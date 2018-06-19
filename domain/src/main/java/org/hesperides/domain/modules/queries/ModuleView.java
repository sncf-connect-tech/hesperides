package org.hesperides.domain.modules.queries;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.TemplateView;

import java.util.List;

@Value
public class ModuleView {
    String name;
    String version;
    boolean isWorkingCopy;
    List<TemplateView> templates;
    List<TechnoView> technos;
    Long versionId;

    public Module toDomainInstance() {
        TemplateContainer.Key moduleKey = new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy));
        return new Module(moduleKey, TemplateView.toDomainInstances(templates, moduleKey), TechnoView.toDomainInstances(technos), versionId);
    }
}
