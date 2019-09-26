package org.hesperides.core.domain.platforms.queries.views;

import lombok.Value;

@Value
public class ModulePlatformView {
    String applicationName;
    String platformName;
    public String toString() {
        return applicationName + "-" + platformName;
    }
}
