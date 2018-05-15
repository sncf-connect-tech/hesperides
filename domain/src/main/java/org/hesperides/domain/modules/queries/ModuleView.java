package org.hesperides.domain.modules.queries;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;

import java.util.List;

@Value
public class ModuleView {
    String name;
    String version;
    boolean workingCopy;
    List<TemplateView> templates;
    List<TechnoView> technos;
    Long versionId;

    public Module toDomainInstance() {
        TemplateContainer.Key moduleKey = new TemplateContainer.Key(name, version, TemplateContainer.getVersionType(workingCopy));
        return new Module(moduleKey, TemplateView.toDomainInstances(templates, moduleKey), TechnoView.toDomainInstances(technos), versionId);
    }
}
