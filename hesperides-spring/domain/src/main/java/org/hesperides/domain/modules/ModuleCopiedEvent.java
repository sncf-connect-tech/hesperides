package org.hesperides.domain.modules;

import lombok.Value;

@Value
public class ModuleCopiedEvent {

    Module.Key moduleKey;

    Module.Key sourceModuleKey;
}
