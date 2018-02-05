package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.modules.Module;

public class TemplateWasNotFoundException extends NotFoundException {
    public TemplateWasNotFoundException(Module.Key moduleKey, String templateName) {
        super("Could not find template in " + moduleKey + "/" + templateName);
    }
}
