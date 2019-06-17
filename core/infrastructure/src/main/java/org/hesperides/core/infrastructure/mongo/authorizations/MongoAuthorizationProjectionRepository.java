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
package org.hesperides.core.infrastructure.mongo.authorizations;

import org.hesperides.core.domain.security.AuthorizationProjectionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class MongoAuthorizationProjectionRepository implements AuthorizationProjectionRepository {

    @Autowired
    private final MongoApplicationAuthoritiesRepository applicationAuthoritiesRepository;

    public MongoAuthorizationProjectionRepository(MongoApplicationAuthoritiesRepository applicationAuthoritiesRepository) {
        this.applicationAuthoritiesRepository = applicationAuthoritiesRepository;
    }

    @Override
    public List<String> getApplicationsForAuthorities(List<String> authorities) {
        return applicationAuthoritiesRepository.findApplicationsWithAuthorities(authorities)
                .stream()
                .map(ApplicationAuthoritiesDocument::getApplication)
                .collect(Collectors.toList());
    }
}
