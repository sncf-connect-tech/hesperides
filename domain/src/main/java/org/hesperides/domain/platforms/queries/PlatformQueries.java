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
package org.hesperides.domain.platforms.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.domain.framework.Queries;
import org.hesperides.domain.platforms.GetApplicationByNameQuery;
import org.hesperides.domain.platforms.GetPlatformByKeyQuery;

import org.hesperides.domain.platforms.SearchPlatformQuery;
import org.hesperides.domain.platforms.SearchApplicationsByNameQuery;

import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.queries.views.ApplicationSearchView;
import org.hesperides.domain.platforms.queries.views.ApplicationView;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.domain.platforms.queries.views.SearchPlatformView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PlatformQueries extends Queries {

    protected PlatformQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public boolean platformExists(Platform.Key platformKey) {
        Optional<PlatformView> optionalPlatformView = querySyncOptional(new GetPlatformByKeyQuery(platformKey), PlatformView.class);
        return optionalPlatformView.isPresent();
    }

    public Optional<PlatformView> getOptionalPlatform(Platform.Key platformKey) {
        return querySyncOptional(new GetPlatformByKeyQuery(platformKey), PlatformView.class);
    }


    public List<SearchPlatformView> search(String applicationName, String platformName) {
        return querySyncList(new SearchPlatformQuery(applicationName, platformName), SearchPlatformView.class);
    }

    public List<ApplicationSearchView> searchApplications(String input) {
        return querySyncList(new SearchApplicationsByNameQuery(input), ApplicationSearchView.class);
    }

    public Optional<ApplicationView> getApplication(String applicationName) {
        return querySyncOptional(new GetApplicationByNameQuery(applicationName), ApplicationView.class);
    }
}
