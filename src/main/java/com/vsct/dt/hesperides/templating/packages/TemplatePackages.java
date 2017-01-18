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

import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 25/02/2015.
 */
public interface TemplatePackages {
    void withAll(Consumer<Template> consumer);

    Set<Template> getAllTemplates(TemplatePackageKey packageInfo);

    Optional<Template> getTemplate(TemplatePackageKey packageInfo, String templateName);

    Optional<Template> getTemplate(String templateNamespace, String templateName);

    Template createTemplateInWorkingCopy(TemplatePackageWorkingCopyKey packageInfo, TemplateData templateData);

    Template updateTemplateInWorkingCopy(TemplatePackageWorkingCopyKey packageInfo, TemplateData templateData);

    void deleteTemplateInWorkingCopy(TemplatePackageWorkingCopyKey packageInfo, String templateName);

    TemplatePackageKey createRelease(TemplatePackageWorkingCopyKey workingCopyKey);

    TemplatePackageKey createWorkingCopyFrom(TemplatePackageWorkingCopyKey workingCopyKey, TemplatePackageKey fromPackageInfos);

    HesperidesPropertiesModel getModel(String name, String version, boolean isWorkingCopy);

    void delete(TemplatePackageKey packageInfo);
}
