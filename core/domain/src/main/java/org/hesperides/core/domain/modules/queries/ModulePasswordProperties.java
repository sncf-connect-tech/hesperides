package org.hesperides.core.domain.modules.queries;

import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;

import java.util.List;

@Value
public class ModulePasswordProperties {
    Module.Key moduleKey;
    List<String> passwords;
}
