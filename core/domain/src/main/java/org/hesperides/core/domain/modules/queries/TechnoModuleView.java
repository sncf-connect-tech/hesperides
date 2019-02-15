package org.hesperides.core.domain.modules.queries;

import lombok.Value;

@Value
public class TechnoModuleView {
    String moduleName;
    String moduleVersion;
    Boolean isWorkingCopy;
}
