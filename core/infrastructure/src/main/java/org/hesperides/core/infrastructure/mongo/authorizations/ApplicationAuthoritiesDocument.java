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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hesperides.core.domain.security.entities.ApplicationAuthorities;
import org.hesperides.core.domain.security.queries.views.ApplicationAuthoritiesView;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

import static org.hesperides.core.infrastructure.Collections.APPLICATION_AUTHORITIES;

@Data
@Document(collection = APPLICATION_AUTHORITIES)
@NoArgsConstructor
public class ApplicationAuthoritiesDocument {
    @Id
    private String id;
    private String applicationName;
    private Map<String, List<String>> authorities;

    public ApplicationAuthoritiesDocument(String id, ApplicationAuthorities applicationAuthorities) {
        this.id = id;
        this.applicationName = applicationAuthorities.getApplicationName();
        this.authorities = applicationAuthorities.getAuthorities();
    }

    ApplicationAuthoritiesView toView() {
        return new ApplicationAuthoritiesView(id, applicationName, authorities);
    }
}
