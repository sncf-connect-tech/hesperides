package com.vsct.dt.hesperides.templating.packages.event;

import com.vsct.dt.hesperides.templating.packages.TemplateCreatedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplateDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageDeletedEvent;
import com.vsct.dt.hesperides.templating.packages.TemplateUpdatedEvent;

/**
 * Created by emeric_martineau on 19/01/2016.
 */
public interface TemplatePackageEventInterface {
    void replayTemplateCreatedEvent(final TemplateCreatedEvent event);

    void replayTemplateUpdatedEvent(final TemplateUpdatedEvent event);

    void replayTemplateDeletedEvent(final TemplateDeletedEvent event);

    void replayTemplatePackageDeletedEvent(final TemplatePackageDeletedEvent event);
}
