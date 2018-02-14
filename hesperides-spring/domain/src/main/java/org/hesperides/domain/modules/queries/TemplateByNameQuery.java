package org.hesperides.domain.modules.queries;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;

@Value
public class TemplateByNameQuery {

    Module.Key moduleKey;
    String templateName;
}
