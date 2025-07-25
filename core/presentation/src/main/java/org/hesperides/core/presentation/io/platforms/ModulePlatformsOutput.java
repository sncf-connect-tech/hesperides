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
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class ModulePlatformsOutput {

    @SerializedName("application_name")
    @JsonProperty("application_name")
    String applicationName;
    @SerializedName("platform_name")
    @JsonProperty("platform_name")
    String platformName;

    public ModulePlatformsOutput(ModulePlatformView modulePlatformView) {
        this.applicationName = modulePlatformView.getApplicationName();
        this.platformName = modulePlatformView.getPlatformName();
    }

    public static List<ModulePlatformsOutput> fromViews(List<ModulePlatformView> modulePlatformViews) {
        return Optional.ofNullable(modulePlatformViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .sorted(Comparator.comparing(ModulePlatformView::getPlatformName))
                .map(ModulePlatformsOutput::new)
                .collect(Collectors.toList());
    }
}
