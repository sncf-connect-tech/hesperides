/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/sncf-connect-tech/hesperides)
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
package org.hesperides.core.domain.security.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.security.GetApplicationDirectoryGroupsQuery;
import org.hesperides.core.domain.security.ResolveDirectoryGroupCNsQuery;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.hesperides.core.domain.security.queries.views.DirectoryGroupsView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ApplicationDirectoryGroupsQueries extends AxonQueries {

    protected ApplicationDirectoryGroupsQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public Optional<ApplicationDirectoryGroupsView> getApplicationDirectoryGroups(String applicationName) {
        return querySyncOptional(new GetApplicationDirectoryGroupsQuery(applicationName), ApplicationDirectoryGroupsView.class);
    }

    public DirectoryGroupsView resolveDirectoryGroupCNs(List<String> directoryGroups) {
        return querySync(new ResolveDirectoryGroupCNsQuery(directoryGroups), DirectoryGroupsView.class);
    }
}
