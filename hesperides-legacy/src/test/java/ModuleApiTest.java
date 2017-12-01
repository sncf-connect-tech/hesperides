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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vsct.dt.hesperides.api.ModuleApi;
import com.vsct.dt.hesperides.api.authentication.SimpleAuthenticator;
import com.vsct.dt.hesperides.api.authentication.User;
import com.vsct.dt.hesperides.domain.modules.ModuleSearchRepository;
import com.vsct.dt.hesperides.infrastructure.elasticsearch.ElasticSearchClient;
import com.vsct.dt.hesperides.infrastructure.elasticsearch.modules.ElasticSearchModuleSearchRepository;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.ClassRule;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModuleApiTest {
    private static final BasicCredentialAuthFilter<User> BASIC_AUTH_HANDLER =
            new BasicCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(new SimpleAuthenticator())
                    .setPrefix("Basic")
                    .setRealm("AUTHENTICATION_PROVIDER")
                    .buildAuthFilter();

    private static ElasticSearchClient elasticSearchClient = mock(ElasticSearchClient.class);

    private static Injector injector = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {
            bind(ModuleSearchRepository.class).to(ElasticSearchModuleSearchRepository.class);
            bind(ElasticSearchClient.class).toInstance(elasticSearchClient);
            bind(MustacheFactory.class).to(DefaultMustacheFactory.class);
        }
    });

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthDynamicFeature(BASIC_AUTH_HANDLER))
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .addResource(injector.getInstance(ModuleApi.class))
            .addResource(injector.getInstance(ElasticSearchClient.class))
            .build();

    //    @Test
    public void test() throws IOException {
        RestClient restClient = mock(RestClient.class);
        Response response = mock(Response.class);
        when(restClient.performRequest(anyString(), anyString(), anyMap(), any(HttpEntity.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(mock(HttpEntity.class));
        when(response.getEntity().getContent()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(elasticSearchClient.getRestClient()).thenReturn(restClient);
        System.out.println(resources.client().target("/toto").request().header("Authorization", "Basic Sm9obl9Eb2U6c2VjcmV0").get().getStatus());
    }
}
