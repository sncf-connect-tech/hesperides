/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.core.presentation.io.platforms;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.hesperides.core.domain.platforms.queries.views.PlatformEventView;
import org.hesperides.core.domain.platforms.queries.views.PlatformEventView.*;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hesperides.core.presentation.io.platforms.PlatformEventOutput.DeployedModuleAddedOutput.DEPLOYED_MODULE_ADDED;
import static org.hesperides.core.presentation.io.platforms.PlatformEventOutput.DeployedModuleRemovedOutput.DEPLOYED_MODULE_REMOVED;
import static org.hesperides.core.presentation.io.platforms.PlatformEventOutput.DeployedModuleUpdatedOutput.DEPLOYED_MODULE_UPDATED;
import static org.hesperides.core.presentation.io.platforms.PlatformEventOutput.PlatformCreatedOutput.PLATFORM_CREATED;
import static org.hesperides.core.presentation.io.platforms.PlatformEventOutput.PlatformVersionUpdatedOutput.PLATFORM_VERSION_UPDATED;

@Value
public class PlatformEventOutput {

    Long timestamp;
    String author;
    List<PlatformChangeOutput> changes;

    public PlatformEventOutput(PlatformEventView platformEventView) {
        this.timestamp = platformEventView.getTimestamp().toEpochMilli();
        this.author = platformEventView.getAuthor();
        this.changes = PlatformChangeOutput.fromsViews(platformEventView.getChanges());
    }

    public static List<PlatformEventOutput> fromViews(List<PlatformEventView> platformEventViews) {
        return platformEventViews.stream()
                .map(PlatformEventOutput::new)
                .collect(toList());
    }

    @Value
    @NonFinal
    public static abstract class PlatformChangeOutput {
        @SerializedName("change_name")
        String changeName;

        public static class Adapter implements JsonDeserializer<PlatformChangeOutput>, JsonSerializer<PlatformChangeOutput> {

            @Override
            public PlatformChangeOutput deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                Class<? extends PlatformChangeOutput> subClassType;
                String changeName = jsonObject.get("change_name").getAsString();
                if (PLATFORM_CREATED.equals(changeName)) {
                    subClassType = PlatformCreatedOutput.class;
                } else if (PLATFORM_VERSION_UPDATED.equals(changeName)) {
                    subClassType = PlatformVersionUpdatedOutput.class;
                } else if (DEPLOYED_MODULE_UPDATED.equals(changeName)) {
                    subClassType = DeployedModuleUpdatedOutput.class;
                } else if (DEPLOYED_MODULE_ADDED.equals(changeName)) {
                    subClassType = DeployedModuleAddedOutput.class;
                } else if (DEPLOYED_MODULE_REMOVED.equals(changeName)) {
                    subClassType = DeployedModuleRemovedOutput.class;
                } else {
                    throw new IllegalArgumentException("Can't find corresponding class for event change " + changeName);
                }
                return context.deserialize(json, subClassType);
            }

            @Override
            public JsonElement serialize(PlatformChangeOutput src, Type typeOfSrc, JsonSerializationContext context) {
                Class<? extends PlatformChangeOutput> subClassType;
                if (src instanceof PlatformCreatedOutput) {
                    subClassType = PlatformCreatedOutput.class;
                } else if (src instanceof PlatformVersionUpdatedOutput) {
                    subClassType = PlatformVersionUpdatedOutput.class;
                } else if (src instanceof DeployedModuleUpdatedOutput) {
                    subClassType = DeployedModuleUpdatedOutput.class;
                } else if (src instanceof DeployedModuleAddedOutput) {
                    subClassType = DeployedModuleAddedOutput.class;
                } else if (src instanceof DeployedModuleRemovedOutput) {
                    subClassType = DeployedModuleRemovedOutput.class;
                } else {
                    throw new IllegalArgumentException("Unknown event change class: " + src.getClass().getName());
                }
                return context.serialize(src, subClassType);
            }
        }

        public static List<PlatformChangeOutput> fromsViews(List<PlatformChangeView> changes) {
            return changes.stream()
                    .map(change -> {
                        PlatformChangeOutput platformChangeOutput;
                        if (change instanceof PlatformCreatedView) {
                            platformChangeOutput = new PlatformCreatedOutput();
                        } else if (change instanceof PlatformVersionUpdatedView) {
                            PlatformVersionUpdatedView platformVersionUpdated = (PlatformVersionUpdatedView) change;
                            platformChangeOutput = new PlatformVersionUpdatedOutput(platformVersionUpdated.getOldVersion(), platformVersionUpdated.getNewVersion());
                        } else if (change instanceof DeployedModuleUpdatedView) {
                            DeployedModuleUpdatedView deployedModuleUpdated = (DeployedModuleUpdatedView) change;
                            platformChangeOutput = new DeployedModuleUpdatedOutput(deployedModuleUpdated.getOldPropertiesPath(), deployedModuleUpdated.getNewPropertiesPath());
                        } else if (change instanceof DeployedModuleAddedView) {
                            DeployedModuleAddedView deployedModuleAdded = (DeployedModuleAddedView) change;
                            platformChangeOutput = new DeployedModuleAddedOutput(deployedModuleAdded.getPropertiesPath());
                        } else if (change instanceof DeployedModuleRemovedView) {
                            DeployedModuleRemovedView deployedModuleRemoved = (DeployedModuleRemovedView) change;
                            platformChangeOutput = new DeployedModuleRemovedOutput(deployedModuleRemoved.getPropertiesPath());
                        } else {
                            throw new RuntimeException("Cant map event " + change);
                        }
                        return platformChangeOutput;
                    }).collect(toList());
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class PlatformCreatedOutput extends PlatformChangeOutput {

        public static final String PLATFORM_CREATED = "platform_created";

        public PlatformCreatedOutput() {
            super(PLATFORM_CREATED);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class PlatformVersionUpdatedOutput extends PlatformChangeOutput {

        public static final String PLATFORM_VERSION_UPDATED = "platform_version_updated";

        @SerializedName("old_version")
        String oldVersion;
        @SerializedName("new_version")
        String newVersion;

        public PlatformVersionUpdatedOutput(String oldVersion, String newVersion) {
            super(PLATFORM_VERSION_UPDATED);
            this.oldVersion = oldVersion;
            this.newVersion = newVersion;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class DeployedModuleUpdatedOutput extends PlatformChangeOutput {

        public static final String DEPLOYED_MODULE_UPDATED = "deployed_module_updated";

        @SerializedName("old_properties_path")
        String oldPropertiesPath;
        @SerializedName("new_properties_path")
        String newPropertiesPath;

        public DeployedModuleUpdatedOutput(String oldPropertiesPath, String newPropertiesPath) {
            super(DEPLOYED_MODULE_UPDATED);
            this.oldPropertiesPath = oldPropertiesPath;
            this.newPropertiesPath = newPropertiesPath;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class DeployedModuleAddedOutput extends PlatformChangeOutput {

        public static final String DEPLOYED_MODULE_ADDED = "deployed_module_added";

        @SerializedName("properties_path")
        String propertiesPath;

        public DeployedModuleAddedOutput(String propertiesPath) {
            super(DEPLOYED_MODULE_ADDED);
            this.propertiesPath = propertiesPath;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class DeployedModuleRemovedOutput extends PlatformChangeOutput {

        public static final String DEPLOYED_MODULE_REMOVED = "deployed_module_removed";

        @SerializedName("properties_path")
        String propertiesPath;

        public DeployedModuleRemovedOutput(String propertiesPath) {
            super(DEPLOYED_MODULE_REMOVED);
            this.propertiesPath = propertiesPath;
        }
    }
}
