package org.hesperides.core.domain.platforms.queries.views;

import lombok.Value;
import org.hesperides.core.domain.platforms.entities.Platform;

@Value
public class ModulePlatformView {

    String applicationName;
    String platformName;

    public String toString() {
        return new Platform.Key(applicationName, platformName).toString();
    }
}
