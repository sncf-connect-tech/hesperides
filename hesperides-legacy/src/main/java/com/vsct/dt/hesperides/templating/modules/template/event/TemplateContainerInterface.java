/*
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
 */

package com.vsct.dt.hesperides.templating.modules.template.event;

import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.template.Template;

import java.util.Set;

/**
 * Created by emeric_martineau on 27/01/2016.
 */
public interface TemplateContainerInterface {
    Template getTemplate(String name);

    void addTemplate(Template template);

    void removeTemplate(String name);

    /**
     * Return list of template.
     *
     * @return set of template
     */
    Set<Template> loadAllTemplate();

    void setModule(Module m);

    Module getModule();

    /**
     * Clear module for reuse object.
     */
    void clear();
}
