package com.vsct.dt.hesperides.applications.event;

import com.vsct.dt.hesperides.applications.*;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public interface PlatformEventBuilderInterface {
    void replayPlatformCreatedEvent(PlatformCreatedEvent event);

    void replayPlatformUpdatedEvent(PlatformUpdatedEvent event);

    void replayPropertiesSavedEvent(PropertiesSavedEvent event);

    void replayPlateformeDeletedEvent(PlatformDeletedEvent event);

    void replaySnapshotTakenEvent(PlatformSnapshotEvent event);

    void replaySnapshotRestoredEvent(PlatformSnapshotRestoreEvent event);
}
