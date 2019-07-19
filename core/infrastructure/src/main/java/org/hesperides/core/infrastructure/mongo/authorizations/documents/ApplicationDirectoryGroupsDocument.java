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
package org.hesperides.core.infrastructure.mongo.authorizations.documents;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.security.entities.ApplicationDirectoryGroups;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import static org.hesperides.core.infrastructure.Collections.APPLICATION_DIRECTORY_GROUPS;

@Data
@Document(collection = APPLICATION_DIRECTORY_GROUPS)
@NoArgsConstructor
public class ApplicationDirectoryGroupsDocument {
    @Id
    private String id;
    private String applicationName;
    private List<String> directoryGroupDNs;

    public ApplicationDirectoryGroupsDocument(String id, ApplicationDirectoryGroups applicationDirectoryGroups) {
        this.id = id;
        this.applicationName = applicationDirectoryGroups.getApplicationName();
        this.directoryGroupDNs = applicationDirectoryGroups.getDirectoryGroupDNs();
    }

    public ApplicationDirectoryGroupsView toView() {
        return new ApplicationDirectoryGroupsView(id, applicationName, directoryGroupDNs);
    }
}
