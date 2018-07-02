package org.hesperides.presentation.io.platforms;

import lombok.Value;
import org.hesperides.domain.platforms.queries.views.SearchPlatformView;

@Value
public class SearchPlatformOutput {
    String platformName;

    public SearchPlatformOutput(SearchPlatformView searchPlatformView) {
        this.platformName = searchPlatformView.getPlatformName();
    }
}
