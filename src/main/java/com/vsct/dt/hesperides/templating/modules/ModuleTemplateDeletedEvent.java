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
 * Created by william_montaz on 10/12/2014.
 */
public final class ModuleTemplateDeletedEvent {
    private final String moduleName;
    private final String moduleVersion;
    private final String templateName;

    @JsonCreator
    public ModuleTemplateDeletedEvent(@JsonProperty("moduleName") final String moduleName,
                                      @JsonProperty("moduleVersion") final String moduleVersion,
                                      @JsonProperty("templateName") final String templateName) {
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
        this.templateName = templateName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public String getTemplateName() {
        return templateName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ModuleTemplateDeletedEvent)) return false;

        ModuleTemplateDeletedEvent that = (ModuleTemplateDeletedEvent) o;

        if (moduleName != null ? !moduleName.equals(that.moduleName) : that.moduleName != null) return false;
        if (moduleVersion != null ? !moduleVersion.equals(that.moduleVersion) : that.moduleVersion != null)
            return false;
        if (templateName != null ? !templateName.equals(that.templateName) : that.templateName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = moduleName != null ? moduleName.hashCode() : 0;
        result = 31 * result + (moduleVersion != null ? moduleVersion.hashCode() : 0);
        result = 31 * result + (templateName != null ? templateName.hashCode() : 0);
        return result;
    }
}
