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

package com.vsct.dt.hesperides.templating.modules.template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;

import java.io.StringReader;

/**
 * Created by william_montaz on 10/07/14.
 */
public final class TemplateSlurper {
    private final String template;
    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public TemplateSlurper(final String template) {
        this.template = template;
    }

    public HesperidesPropertiesModel generatePropertiesScope() {
        if (template != null) {
            Mustache mustache = mustacheFactory.compile(new StringReader(template), "something");
            return new HesperidesPropertiesModel(mustache.getCodes());
        } else return HesperidesPropertiesModel.empty();
    }
}
