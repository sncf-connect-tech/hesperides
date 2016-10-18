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

package com.vsct.dt.hesperides.indexation.mapper;

import com.vsct.dt.hesperides.indexation.model.ModuleIndexation;
import com.vsct.dt.hesperides.indexation.model.TemplatePackageIndexation;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.Techno;

import java.util.stream.Collectors;

/**
 * Created by william_montaz on 09/12/2014.
 */
public class ModuleMapper {

    public static ModuleIndexation toModuleIndexation(final Module module) {
        return new ModuleIndexation(
                module.getName(),
                module.getVersion(),
                module.isWorkingCopy(),
                module.getTechnos().stream().map(templatePackage -> toTemplatePackageIndexation(templatePackage)).collect(Collectors.toList())
        );
    }

    private static TemplatePackageIndexation toTemplatePackageIndexation(final Techno templatePackage) {
        return new TemplatePackageIndexation(
                templatePackage.getName(),
                templatePackage.getVersion(),
                templatePackage.isWorkingCopy()
        );
    }

}
