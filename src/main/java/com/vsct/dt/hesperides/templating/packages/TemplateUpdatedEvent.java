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

package com.vsct.dt.hesperides.templating.packages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsct.dt.hesperides.templating.Template;

import java.util.Objects;

/**
 * Created by william_montaz on 13/10/2014.
 */
public final class TemplateUpdatedEvent {
    private final Template updated;

    @JsonCreator
    public TemplateUpdatedEvent(@JsonProperty("updated") final Template updated) {
        this.updated = updated;
    }

    public Template getUpdated() {
        return updated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(updated);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TemplateUpdatedEvent other = (TemplateUpdatedEvent) obj;
        return Objects.equals(this.updated, other.updated);
    }
}
