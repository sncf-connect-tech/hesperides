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
package org.hesperides.core.domain.platforms.queries;

import org.axonframework.queryhandling.QueryExecutionException;
import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.exceptions.InexistantPlatformAtTimeException;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Component
public class PlatformQueries extends AxonQueries {

    private QueryGateway queryGateway;

    protected PlatformQueries(QueryGateway queryGateway) {
        super(queryGateway);
        this.queryGateway = queryGateway;
    }

    public Optional<String> getOptionalPlatformId(Platform.Key platformKey) {
        return querySyncOptional(new GetPlatformIdFromKeyQuery(platformKey), String.class);
    }

    public Optional<String> getOptionalPlatformIdFromEvents(Platform.Key platformKey) {
        return querySyncOptional(new GetPlatformIdFromEvents(platformKey), String.class);
    }

    public Optional<PlatformView> getOptionalPlatform(String platformId) {
        return querySyncOptional(new GetPlatformByIdQuery(platformId), PlatformView.class);
    }

    public Optional<PlatformView> getOptionalPlatform(Platform.Key platformKey) {
        return querySyncOptional(new GetPlatformByKeyQuery(platformKey), PlatformView.class);
    }

    public PlatformView getPlatformAtPointInTime(String platformId, long timestamp) {
        try {
            return queryGateway.query(new GetPlatformAtPointInTimeQuery(platformId, timestamp), PlatformView.class).get();
        } catch (ExecutionException | InterruptedException error) {
            if (error.getCause() instanceof InexistantPlatformAtTimeException) {
                throw (InexistantPlatformAtTimeException) error.getCause();
            }
            throw new QueryExecutionException(error.getMessage(), error);
        }
    }

    public boolean platformExists(Platform.Key platformKey) {
        return querySync(new PlatformExistsByKeyQuery(platformKey), Boolean.class);
    }

    public Optional<ApplicationView> getApplication(String applicationName, boolean hidePlatformsModules) {
        return querySyncOptional(new GetApplicationByNameQuery(applicationName, hidePlatformsModules), ApplicationView.class);
    }

    public List<String> getInstancesModel(final Platform.Key platformKey, final String propertiesPath) {
        return querySyncList(new GetInstancesModelQuery(platformKey, propertiesPath), String.class);
    }

    public List<ModulePlatformView> getPlatformsUsingModule(Module.Key moduleKey) {
        return querySyncList(new GetPlatformsUsingModuleQuery(moduleKey), ModulePlatformView.class);
    }

    public List<SearchApplicationResultView> getApplicationNames() {
        return querySyncList(new GetApplicationNamesQuery(), SearchApplicationResultView.class);
    }

    public List<SearchApplicationResultView> searchApplications(String applicationName) {
        return querySyncList(new SearchApplicationsQuery(applicationName), SearchApplicationResultView.class);
    }

    public List<SearchPlatformResultView> searchPlatforms(String applicationName, String platformName) {
        return querySyncList(new SearchPlatformsQuery(applicationName, platformName), SearchPlatformResultView.class);
    }

    public Long getPropertiesVersionId(final String platformId, final String propertiesPath, final Long timestamp) {
        return querySync(new GetPropertiesVersionIdQuery(platformId, propertiesPath, timestamp == null ? -1 : timestamp), Long.class);
    }

    public List<AbstractValuedPropertyView> getDeployedModuleProperties(final String platformId, final String propertiesPath, final Long timestamp) {
        return querySyncList(new GetDeployedModulePropertiesQuery(platformId, propertiesPath, timestamp == null ? -1 : timestamp), AbstractValuedPropertyView.class);
    }

    public Optional<Long> getGlobalPropertiesVersionId(final Platform.Key platformKey) {
        return querySyncOptional(new GetGlobalPropertiesVersionIdQuery(platformKey), Long.class);
    }

    public List<ValuedPropertyView> getGlobalProperties(final Platform.Key platformKey) {
        return querySyncList(new GetGlobalPropertiesQuery(platformKey), ValuedPropertyView.class);
    }

    public boolean deployedModuleExists(Platform.Key platformKey, Module.Key moduleKey, String modulePath) {
        return querySync(new DeployedModuleExistsQuery(platformKey, moduleKey, modulePath), Boolean.class);
    }

    public boolean instanceExists(Platform.Key platformKey, Module.Key moduleKey, String modulePath, String instanceName) {
        return querySync(new InstanceExistsQuery(platformKey, moduleKey, modulePath, instanceName), Boolean.class);
    }

    public boolean applicationExists(String applicationName) {
        return querySync(new ApplicationExistsQuery(applicationName), Boolean.class);
    }

    public List<ApplicationView> getAllApplicationsDetail() {
        return querySyncList(new GetAllApplicationsDetailQuery(), ApplicationView.class);
    }
}
