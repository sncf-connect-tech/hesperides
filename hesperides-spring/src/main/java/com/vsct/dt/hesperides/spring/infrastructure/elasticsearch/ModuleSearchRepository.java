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
package com.vsct.dt.hesperides.spring.infrastructure.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ModuleSearchRepository implements com.vsct.dt.hesperides.spring.domain.ModuleSearchRepository {

    @Autowired
    ModuleRepository moduleRepository;

    @Override
    public List<com.vsct.dt.hesperides.spring.domain.Module> getModules() {
        List<com.vsct.dt.hesperides.spring.domain.Module> modules = new ArrayList<>();
        for (Module elasticsearchModule : moduleRepository.findAll()) {
            modules.add(elasticsearchModule.toDomainModule());
        }
        return modules;
    }
}
