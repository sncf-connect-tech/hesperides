/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.presentation.io.OnlyPrintableCharacters;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Value
@AllArgsConstructor
public class PlatformIO {

    @OnlyPrintableCharacters(subject = "platform_name")
    @SerializedName("platform_name")
    @JsonProperty("platform_name")
    String platformName;
    @OnlyPrintableCharacters(subject = "application_name")
    @SerializedName("application_name")
    @JsonProperty("application_name")
    String applicationName;
    @NotEmpty
    @SerializedName("application_version")
    @JsonProperty("application_version")
    String version;
    @SerializedName("production")
    @JsonProperty("production")
    Boolean isProductionPlatform;
    @SerializedName("modules")
    @JsonProperty("modules")
    List<DeployedModuleIO> deployedModules;
    @NotNull
    @SerializedName("version_id")
    @JsonProperty("version_id")
    Long versionId;
    @SerializedName("has_passwords")
    @JsonProperty("has_passwords")
    Boolean hasPasswords;

    /**
     * Si la propriété hasPasswords est null, cela signifie que cette information
     * n'a pas été demandée donc on ne la sérialise pas pour ne pas créer de confusion.
     */
    public static class Serializer implements JsonSerializer<PlatformIO> {
        @Override
        public JsonElement serialize(PlatformIO src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            JsonObject jsonObject = (JsonObject) gson.toJsonTree(src);
            if (src.getHasPasswords() == null) {
                jsonObject.remove("has_passwords");
            }
            return jsonObject;
        }
    }

    public PlatformIO(PlatformView platformView) {
        platformName = platformView.getPlatformName();
        applicationName = platformView.getApplicationName();
        version = platformView.getVersion();
        isProductionPlatform = platformView.isProductionPlatform();
        deployedModules = DeployedModuleIO.fromActiveDeployedModuleViews(platformView.findActiveDeployedModules());
        versionId = platformView.getVersionId();
        this.hasPasswords = platformView.getHasPasswords();
    }

    public Platform toDomainInstance() {
        return new Platform(
                new Platform.Key(applicationName, platformName),
                version,
                isProductionPlatform != null ? isProductionPlatform : false,
                versionId,
                DeployedModuleIO.toDomainInstances(deployedModules),
                null, // Les propriétés globales ne sont pas présente au niveau des IO de plateforme et donc du payload json de réponse, le domain se charge donc de gérer cela
                Collections.emptyList()
        );
    }

    static List<PlatformIO> fromPlatformViews(List<PlatformView> platformViews) {
        return Optional.ofNullable(platformViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PlatformIO::new)
                .collect(toList());
    }
}
