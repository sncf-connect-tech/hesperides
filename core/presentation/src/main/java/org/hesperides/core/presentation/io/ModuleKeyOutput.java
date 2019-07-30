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
package org.hesperides.core.presentation.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class ModuleKeyOutput {

    @SerializedName("module_name")
    @JsonProperty("module_name")
    String moduleName;
    @SerializedName("module_version")
    @JsonProperty("module_version")
    String moduleVersion;
    @SerializedName("working_copy")
    @JsonProperty("working_copy")
    Boolean isWorkingCopy;

    public ModuleKeyOutput(TemplateContainerKeyView keyView) {
        this.moduleName = keyView.getName();
        this.moduleVersion = keyView.getVersion();
        this.isWorkingCopy = keyView.getIsWorkingCopy();
    }

    public static List<ModuleKeyOutput> fromViews(List<TemplateContainerKeyView> keyViews) {
        return Optional.ofNullable(keyViews)
                .orElseGet(Collections::emptyList)
                .stream()
                .sorted(Comparator.comparing(TemplateContainerKeyView::getName))
                .map(ModuleKeyOutput::new)
                .collect(Collectors.toList());
    }
}
