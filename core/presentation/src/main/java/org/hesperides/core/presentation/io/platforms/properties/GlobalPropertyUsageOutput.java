package org.hesperides.core.presentation.io.platforms.properties;

import lombok.Value;

/**
 * This class represents the global property usage detail.
 */
@Value
public class GlobalPropertyUsageOutput {

    boolean inModel;
    String path;
}
