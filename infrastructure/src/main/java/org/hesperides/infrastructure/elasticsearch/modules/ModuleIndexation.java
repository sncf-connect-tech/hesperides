/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.hesperides.infrastructure.elasticsearch.modules;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.queries.ModuleView;

import java.util.List;

@Value
public final class ModuleIndexation {

    private final String name;
    private final String version;
    @SerializedName("working_copy")
    private final boolean workingCopy;
    private final List<TemplatePackageIndexation> technos;

//    @JsonCreator
//    public ModuleIndexation(@JsonProperty("name") final String name,
//                            @JsonProperty("version") final String version,
//                            @JsonProperty("working_copy") final boolean workingCopy,
//                            @JsonProperty("technos") final List<TemplatePackageIndexation> technos) {
//        this.name = name;
//        this.version = version;
//        this.workingCopy = workingCopy;
//        this.technos = technos == null ? ImmutableList.of() : Lists.newArrayList(technos);
//    }

    public ModuleView toModuleView() {
        return new ModuleView(name, version, workingCopy, 0L);
    }

    public String toModuleTypeView() {
        return workingCopy ? "workingcopy" : "release";
    }
}
