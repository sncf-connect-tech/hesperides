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
import com.google.common.collect.ImmutableSet;
import com.vsct.dt.hesperides.templating.modules.template.Template;

import java.util.Set;

/**
 * Created by william_montaz on 04/12/2014.
 */
public final class ModuleCreatedEvent {
    private final Module                 moduleCreated;
    private final ImmutableSet<Template> templates;

    @JsonCreator
    public ModuleCreatedEvent(@JsonProperty("moduleCreated") final Module moduleCreated,
                              @JsonProperty("templates") final Set<Template> templates) {
        this.moduleCreated = moduleCreated;
        this.templates = ImmutableSet.copyOf(templates);
    }

    public Module getModuleCreated() {
        return moduleCreated;
    }

    public ImmutableSet<Template> getTemplates() {
        return templates;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleCreatedEvent)) return false;

        ModuleCreatedEvent that = (ModuleCreatedEvent) o;

        if (moduleCreated != null ? !moduleCreated.equals(that.moduleCreated) : that.moduleCreated != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return moduleCreated != null ? moduleCreated.hashCode() : 0;
    }
}
