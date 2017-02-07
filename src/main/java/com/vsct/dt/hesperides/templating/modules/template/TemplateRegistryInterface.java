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

package com.vsct.dt.hesperides.templating.modules.template;

import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Created by emeric_martineau on 18/01/2016.
 */
public interface TemplateRegistryInterface {
    Collection<Template> allTemplates();

    Optional<Template> getTemplate(String namespace, String name);

    Optional<Template> getTemplate(TemplatePackageKey packageKey, String name);

    boolean existsTemplate(String namespace, String name);

    void createOrUpdateTemplate(Template template);

    void deleteTemplate(String namespace, String name);

    Set<Template> getAllTemplatesForNamespace(String namespace);

    Set<Template> getAllTemplates(TemplatePackageKey packageKey);

    Set<Template> getAllTemplates();

    boolean templateHasNamespace(String namespace);

    /**
     * Remove item from cache.
     *
     * @param packageKey
     */
    void removeFromCache(TemplatePackageKey packageKey);

    /**
     * Remove all data in cache.
     */
    void removeAllCache();
}
