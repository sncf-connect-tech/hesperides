package org.hesperides.core.domain.platforms.queries.views.events;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;

@Value
public class UpdatedPlatformEventsView {
    Long timestamp;
    List<UpdatedPlatformEventView> events;

    @Value
    @NonFinal
    public abstract class UpdatedPlatformEventView {
        UpdatedPlatformEventType eventType;

        public UpdatedPlatformEventView(UpdatedPlatformEventType updatedPlatformEventType) {
            this.eventType = updatedPlatformEventType;
        }
    }

    @Value
    public class PlatformVersionUpdatedEventView extends UpdatedPlatformEventView {
        String oldVersion;
        String newVersion;

        public PlatformVersionUpdatedEventView(UpdatedPlatformEventType updatedPlatformEventType,
                                               String oldVersion,
                                               String newVersion) {
            super(updatedPlatformEventType);
            this.oldVersion = oldVersion;
            this.newVersion = newVersion;
        }
    }

    @Value
    public class DeployedModuleVersionUpdatedEventView extends UpdatedPlatformEventView {
        String propertiesPath;
        String oldVersion;
        String newVersion;

        public DeployedModuleVersionUpdatedEventView(UpdatedPlatformEventType updatedPlatformEventType,
                                            String propertiesPath,
                                            String oldVersion,
                                            String newVersion) {
            super(updatedPlatformEventType);
            this.propertiesPath = propertiesPath;
            this.oldVersion = oldVersion;
            this.newVersion = newVersion;
        }
    }

    @Value
    public class DeployedModuleAddedEventView extends UpdatedPlatformEventView {
        String propertiesPath;

        public DeployedModuleAddedEventView(UpdatedPlatformEventType updatedPlatformEventType,
                                            String propertiesPath) {
            super(updatedPlatformEventType);
            this.propertiesPath = propertiesPath;
        }
    }

    @Value
    public class DeployedModuleRemovedEventView extends UpdatedPlatformEventView {
        String propertiesPath;

        public DeployedModuleRemovedEventView(UpdatedPlatformEventType updatedPlatformEventType,
                                              String propertiesPath) {
            super(updatedPlatformEventType);
            this.propertiesPath = propertiesPath;
        }
    }

    public enum UpdatedPlatformEventType {
        PLATFORM_VERSION_UPDATED,
        DEPLOYED_MODULE_VERSION_UPDATED,
        DEPLOYED_MODULE_ADDED,
        DEPLOYED_MODULE_REMOVED;
    }
}
