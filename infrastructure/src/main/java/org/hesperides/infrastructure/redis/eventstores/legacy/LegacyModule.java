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
package org.hesperides.infrastructure.redis.eventstores.legacy;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;

import java.util.Collection;

/**
 * L'utilisation de Gson pour sérialiser/désérialiser permet l'utilisation de Lombok
 */
@Value
public class LegacyModule {
    String name;
    String version;
    @SerializedName("working_copy")
    boolean workingCopy;
    Collection technos;
    @SerializedName("version_id")
    Long versionId;

    public Module.Type getModuleType() {
        return workingCopy ? Module.Type.workingcopy : Module.Type.release;
    }
}
