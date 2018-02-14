package org.hesperides.domain.modules.queries;

import lombok.Value;

@Value
public class ModuleView {
    String name;
    String version;
    boolean working_copy;
    long version_id;
}
