package org.hesperides.domain.modules.queries;

import lombok.Value;

import java.util.List;

@Value
public class ModuleView {
    String name;
    String version;
    boolean working_copy;
    long version_id;
}
