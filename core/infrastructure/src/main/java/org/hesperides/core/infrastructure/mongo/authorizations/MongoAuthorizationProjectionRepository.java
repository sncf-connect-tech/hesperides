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

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.authorizations.ApplicationAuthoritiesCreatedEvent;
import org.hesperides.core.domain.authorizations.ApplicationAuthoritiesUpdatedEvent;
import org.hesperides.core.domain.authorizations.GetApplicationAuthoritiesQuery;
import org.hesperides.core.domain.security.AuthorizationProjectionRepository;
import org.hesperides.core.domain.security.queries.views.ApplicationAuthoritiesView;
import org.hesperides.core.infrastructure.mongo.MongoProjectionRepositoryConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.HasProfile.isProfileActive;
import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;
import static org.hesperides.core.infrastructure.Collections.APPLICATION_AUTHORITIES;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoAuthorizationProjectionRepository implements AuthorizationProjectionRepository {

    private final MongoApplicationAuthoritiesRepository applicationAuthoritiesRepository;
    private final MongoTemplate mongoTemplate;
    private final Environment environment;

    public MongoAuthorizationProjectionRepository(MongoApplicationAuthoritiesRepository applicationAuthoritiesRepository, MongoTemplate mongoTemplate, Environment environment) {
        this.applicationAuthoritiesRepository = applicationAuthoritiesRepository;
        this.mongoTemplate = mongoTemplate;
        this.environment = environment;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (isProfileActive(environment, MONGO)) {
            MongoProjectionRepositoryConfiguration.ensureCaseInsensitivity(mongoTemplate, APPLICATION_AUTHORITIES);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onApplicationAuthoritiesCreatedEvent(ApplicationAuthoritiesCreatedEvent event) {
        ApplicationAuthoritiesDocument applicationAuthoritiesDocument = new ApplicationAuthoritiesDocument(event.getId(), event.getApplicationAuthorities());
        applicationAuthoritiesRepository.save(applicationAuthoritiesDocument);
    }

    @EventHandler
    @Override
    public void onApplicationAuthoritiesUpdatedEvent(ApplicationAuthoritiesUpdatedEvent event) {
        ApplicationAuthoritiesDocument applicationAuthoritiesDocument = new ApplicationAuthoritiesDocument(event.getId(), event.getApplicationAuthorities());
        applicationAuthoritiesRepository.save(applicationAuthoritiesDocument);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<ApplicationAuthoritiesView> getApplicationAuthoritiesQuery(GetApplicationAuthoritiesQuery query) {
        return applicationAuthoritiesRepository.findByApplicationName(query.getApplicationName())
                .map(ApplicationAuthoritiesDocument::toView);
    }

    @Override
    public List<String> getApplicationsForAuthorities(List<String> authorities) {
        return applicationAuthoritiesRepository.findApplicationsWithAuthorities(authorities)
                .stream()
                .map(ApplicationAuthoritiesDocument::getApplicationName)
                .collect(Collectors.toList());
    }
}
