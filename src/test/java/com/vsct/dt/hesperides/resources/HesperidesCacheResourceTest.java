/*
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.vsct.dt.hesperides.resources;

import javax.ws.rs.core.Response.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;
import tests.type.UnitTests;

import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.cache.HesperidesCacheResource;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;

import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorApplicationAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorModuleAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorTemplatePackagesAggregate;

/**
 * Created by emeric_martineau on 23/01/17.
 */
@Category(UnitTests.class)
public class HesperidesCacheResourceTest extends AbstractTechUserResourceTest {

    private static final ModulesAggregate MODULES_AGGREGATE = mock(ModulesAggregate.class);
    private static final TemplatePackagesAggregate TEMPLATE_PACKAGES_AGGREGATE = mock(TemplatePackagesAggregate.class);
    private static final ApplicationsAggregate APPLICATIONS_AGGREGATE = mock(ApplicationsAggregate.class);
    private static final CacheGeneratorTemplatePackagesAggregate CACHE_GENERATOR_TEMPLATE_PACKAGES_AGGREGATE = mock
            (CacheGeneratorTemplatePackagesAggregate.class);
    private static final CacheGeneratorModuleAggregate CACHE_GENERATOR_MODULE_AGGREGATE = mock(CacheGeneratorModuleAggregate.class);
    private static final CacheGeneratorApplicationAggregate CACHE_GENERATOR_APPLICATION_AGGREGATE = mock(CacheGeneratorApplicationAggregate.class);

    public static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @ClassRule
    public static ResourceTestRule authResources = createAuthenticationResource(new HesperidesCacheResource(TEMPLATE_PACKAGES_AGGREGATE,
            MODULES_AGGREGATE, APPLICATIONS_AGGREGATE, CACHE_GENERATOR_TEMPLATE_PACKAGES_AGGREGATE, CACHE_GENERATOR_MODULE_AGGREGATE,
            CACHE_GENERATOR_APPLICATION_AGGREGATE));


    @Before
    public void setup() {
        reset(MODULES_AGGREGATE);
        reset(APPLICATIONS_AGGREGATE);
        reset(TEMPLATE_PACKAGES_AGGREGATE);
    }

    @Override
    protected ResourceTestRule getAuthResources() {
        return authResources;
    }

    @Test
    public void should_return_403_forbiden_when_clear_applications_caches() {
        assertThat(
            withNoTechAuth("/cache/applications")
                    .delete()
                    .getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void should_return_works_when_clear_applications_caches() {
        assertThat(
            withTechAuth("/cache/applications")
                    .delete()
                    .getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void should_return_403_forbiden_when_clear_modules_caches() {
        assertThat(
            withNoTechAuth("/cache/modules")
                    .delete()
                    .getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void should_return_works_when_clear_modules_caches() {
        assertThat(
            withTechAuth("/cache/modules")
                    .delete()
                    .getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    @Test
    public void should_return_403_forbiden_when_clear_templates_packages_caches() {
        assertThat(
            withNoTechAuth("/cache/templates/packages")
                    .delete()
                    .getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void should_return_works_when_clear_templates_packages_caches() {
        assertThat(
            withTechAuth("/cache/templates/packages")
                    .delete()
                    .getStatus()).isEqualTo(Status.OK.getStatusCode());
    }
}
