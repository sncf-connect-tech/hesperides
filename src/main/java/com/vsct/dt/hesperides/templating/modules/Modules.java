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

import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 25/02/2015.
 */
public interface Modules {
    void withAll(Consumer<Module> consumer);

    void withAllTemplates(Consumer<Template> consumer);

    Collection<Module> getAllModules();

    Optional<Module> getModule(ModuleKey moduleKey);

    List<Template> getAllTemplates(ModuleKey moduleKey);

    Optional<Template> getTemplate(ModuleKey moduleKey, String templateName);

    Optional<HesperidesPropertiesModel> getModel(ModuleKey moduleKey);

    Module createWorkingCopy(Module module);

    Module updateWorkingCopy(Module module);

    Template updateTemplateInWorkingCopy(ModuleWorkingCopyKey moduleKey, TemplateData templateData);

    Template createTemplateInWorkingCopy(ModuleWorkingCopyKey moduleKey, TemplateData templateData);

    void deleteTemplateInWorkingCopy(ModuleWorkingCopyKey moduleKey, String templateName);

    Module createWorkingCopyFrom(ModuleWorkingCopyKey newModuleKey, ModuleKey fromModuleKey);

    Module createRelease(ModuleWorkingCopyKey moduleKey, String releaseVersion);

    Module createRelease(ModuleWorkingCopyKey moduleKey, String releaseVersion, String nextVersion);

    void delete(ModuleKey moduleKey);
}
