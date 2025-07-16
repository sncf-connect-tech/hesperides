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
package org.hesperides.core.infrastructure.mongo.authorizations;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.SpringProfiles;
import org.hesperides.core.domain.security.ApplicationDirectoryGroupsCreatedEvent;
import org.hesperides.core.domain.security.ApplicationDirectoryGroupsUpdatedEvent;
import org.hesperides.core.domain.security.AuthorizationProjectionRepository;
import org.hesperides.core.domain.security.GetApplicationDirectoryGroupsQuery;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.hesperides.core.infrastructure.mongo.MongoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;
import static org.hesperides.core.infrastructure.mongo.Collections.APPLICATION_DIRECTORY_GROUPS;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoAuthorizationProjectionRepository implements AuthorizationProjectionRepository {

    private final MongoApplicationDirectoryGroupsRepository applicationDirectoryGroupsRepository;
    private final MongoTemplate mongoTemplate;
    private final SpringProfiles springProfiles;

    public MongoAuthorizationProjectionRepository(MongoApplicationDirectoryGroupsRepository applicationDirectoryGroupsRepository,
                                                  MongoTemplate mongoTemplate,
                                                  SpringProfiles springProfiles) {
        this.applicationDirectoryGroupsRepository = applicationDirectoryGroupsRepository;
        this.mongoTemplate = mongoTemplate;
        this.springProfiles = springProfiles;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (springProfiles.isActive(MONGO)) {
            MongoConfiguration.ensureCaseInsensitivity(mongoTemplate, APPLICATION_DIRECTORY_GROUPS);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onApplicationDirectoryGroupsCreatedEvent(ApplicationDirectoryGroupsCreatedEvent event) {
        ApplicationDirectoryGroupsDocument applicationDirectoryGroupsDocument = new ApplicationDirectoryGroupsDocument(event.getId(), event.getApplicationDirectoryGroups());
        applicationDirectoryGroupsRepository.save(applicationDirectoryGroupsDocument);
    }

    @EventHandler
    @Override
    public void onApplicationDirectoryGroupsUpdatedEvent(ApplicationDirectoryGroupsUpdatedEvent event) {
        ApplicationDirectoryGroupsDocument applicationDirectoryGroupsDocument = new ApplicationDirectoryGroupsDocument(event.getId(), event.getApplicationDirectoryGroups());
        applicationDirectoryGroupsRepository.save(applicationDirectoryGroupsDocument);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<ApplicationDirectoryGroupsView> onGetApplicationDirectoryGroupsQuery(GetApplicationDirectoryGroupsQuery query) {
        return applicationDirectoryGroupsRepository.findByApplicationName(query.getApplicationName())
                .map(ApplicationDirectoryGroupsDocument::toView);
    }

    @Override
    public List<String> getApplicationsWithDirectoryGroups(List<String> directoryGroups) {
        return applicationDirectoryGroupsRepository.findApplicationsWithDirectoryGroups(directoryGroups)
                .stream()
                .map(ApplicationDirectoryGroupsDocument::getApplicationName)
                .collect(Collectors.toList());
    }
}
