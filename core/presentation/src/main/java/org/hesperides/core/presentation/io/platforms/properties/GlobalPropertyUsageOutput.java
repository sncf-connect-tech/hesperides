package org.hesperides.core.presentation.io.platforms.properties;

import lombok.Value;

/**
 * This class represents the global property usage detail.
 */
@Value
public class GlobalPropertyUsageOutput {

    private boolean inModel;
    private String path;
}
