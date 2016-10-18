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

package com.vsct.dt.hesperides.templating.models;

import com.vsct.dt.hesperides.templating.TemplateRegistry;

/**
 * Created by william_montaz on 10/12/2014.
 */
public class Models {

    private final TemplateRegistry templateRegistry;

    public Models(final TemplateRegistry templateRegistry) {
        this.templateRegistry = templateRegistry;
    }

    public HesperidesPropertiesModel getPropertiesModel(final String namespace) {
        return templateRegistry.getAllForNamespace(namespace).stream().map(template -> {
            return template.generatePropertiesModel();
        }).reduce(HesperidesPropertiesModel.empty(), (a, b) -> a.merge(b));
    }

}
