/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.presentation;

import org.hesperides.domain.Module;
import org.hesperides.domain.ModuleSearchRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/modules")
public class ModuleController {

    private final ModuleSearchRepository moduleSearchRepository;

    public ModuleController(ModuleSearchRepository moduleSearchRepository) {
        this.moduleSearchRepository = moduleSearchRepository;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Collection<String> getModules() {
        return moduleSearchRepository.getModules().stream()
                .map(Module::getName)
                .collect(Collectors.toSet());
    }
}
