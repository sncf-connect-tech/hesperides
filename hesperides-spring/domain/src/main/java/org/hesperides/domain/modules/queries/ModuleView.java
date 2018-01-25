package org.hesperides.domain.modules.queries;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Value;
import org.hesperides.domain.modules.commands.Module;

@Value
public class ModuleView {

    @JsonUnwrapped
    Module.Key key;
}
