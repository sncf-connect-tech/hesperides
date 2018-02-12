package org.hesperides.domain.modules.queries;

import lombok.Value;
import org.hesperides.domain.modules.Module;

@Value
public class TemplateByNameQuery {

    Module.Key moduleKey;
    String templateName;
}
