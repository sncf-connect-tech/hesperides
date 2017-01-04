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

package com.vsct.dt.hesperides.templating.modules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by william_montaz on 04/12/2014.
 */
public final class ModuleWorkingCopyUpdatedEvent {
    private final Module updated;

    @JsonCreator
    protected ModuleWorkingCopyUpdatedEvent(@JsonProperty("updated") final Module updated) {
        this.updated = updated;
    }

    public Module getUpdated() {
        return updated;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleWorkingCopyUpdatedEvent)) return false;

        ModuleWorkingCopyUpdatedEvent that = (ModuleWorkingCopyUpdatedEvent) o;

        if (updated != null ? !updated.equals(that.updated) : that.updated != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return updated != null ? updated.hashCode() : 0;
    }
}
