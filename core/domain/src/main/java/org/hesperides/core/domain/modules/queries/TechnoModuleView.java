package org.hesperides.core.domain.modules.queries;

import lombok.Value;

import static org.hesperides.core.domain.templatecontainers.entities.TemplateContainer.getVersionType;

@Value
public class TechnoModuleView {
    String moduleName;
    String moduleVersion;
    Boolean isWorkingCopy;

    @Override
    public String toString() {
        return moduleName + "/" + moduleVersion + "/" + getVersionType(isWorkingCopy);
    }
}
