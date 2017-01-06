/*
 *
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
 *
 */

package com.vsct.dt.hesperides.resources;

import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.exception.runtime.ForbiddenOperationException;
import com.vsct.dt.hesperides.security.UserContext;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 27/02/2015.
 */
public class PermissionAwareApplicationsProxyTest {

    ApplicationsAggregate            applicationsAggregate = mock(ApplicationsAggregate.class);
    UserContext                      userContext           = mock(UserContext.class);
    PermissionAwareApplicationsProxy applications          = new PermissionAwareApplicationsProxy(applicationsAggregate, userContext);

    User prodUser    = new User("prod", true);
    User nonProdUser = new User("non_prod", false);

    private final String comment = "Test comment";

    @Before
    public void setUp() {
        reset(applicationsAggregate);
        reset(userContext);
    }

    @Test(expected = ForbiddenOperationException.class)
    public void should_not_allow_non_prod_user_to_create_production_platform() {
        when(userContext.getCurrentUser()).thenReturn(nonProdUser);

        PlatformKey platformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        try {
            applications.createPlatform(builder2.build());
        } catch (ForbiddenOperationException e) {
            verifyZeroInteractions(applicationsAggregate);
            throw e;
        }
    }

    @Test(expected = ForbiddenOperationException.class)
    public void should_not_allow_non_prod_user_to_create_production_platform_from_another_platform() {
        when(userContext.getCurrentUser()).thenReturn(nonProdUser);

        PlatformKey platformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        //This way we show that copying from a production paltform is ok but only if the target platfm is non production
        PlatformKey fromPlatformKey = PlatformKey.withName("from_pltfm")
                .withApplicationName("app")
                .build();

        try{
            applications.createPlatformFromExistingPlatform(builder2.build(), fromPlatformKey);
        } catch(ForbiddenOperationException e){
            verifyZeroInteractions(applicationsAggregate);
            throw e;
        }
    }

    @Test
    public void should_allow_prod_user_to_create_production_platform() {
        when(userContext.getCurrentUser()).thenReturn(prodUser);

        PlatformKey platformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder2.build();

        when(applicationsAggregate.createPlatform(platform)).thenReturn(platform);

        PlatformData created = applications.createPlatform(platform);

        assertThat(created).isEqualTo(platform);
        verify(applicationsAggregate).createPlatform(platform);
        verifyNoMoreInteractions(applicationsAggregate);
    }

    @Test
    public void should_allow_prod_user_to_create_production_platform_from_another_platform() {
        when(userContext.getCurrentUser()).thenReturn(prodUser);

        PlatformKey platformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();

        PlatformKey fromPlatformKey = PlatformKey.withName("from_pltfm")
                .withApplicationName("app")
                .build();


        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder2.build();
        Optional<PlatformData> fromPlatform = Optional.of(builder2.build());

        when(applicationsAggregate.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);
        when(applicationsAggregate.getPlatform(fromPlatformKey)).thenReturn(fromPlatform);

        PlatformData created = applications.createPlatformFromExistingPlatform(platform, fromPlatformKey);

        assertThat(created).isEqualTo(platform);
        verify(applicationsAggregate).createPlatformFromExistingPlatform(platform, fromPlatformKey);
        verify(applicationsAggregate).getPlatform(fromPlatformKey);
        verifyNoMoreInteractions(applicationsAggregate);
    }

    @Test(expected = ForbiddenOperationException.class)
    public void should_not_allow_regular_user_to_create_platform_from_production_platform() {
        when(userContext.getCurrentUser()).thenReturn(nonProdUser);

        PlatformKey platformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();

        PlatformKey fromPlatformKey = PlatformKey.withName("from_pltfm")
                .withApplicationName("app")
                .build();


        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();
        Optional<PlatformData> fromPlatform = Optional.of(builder2.build());

        when(applicationsAggregate.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);
        when(applicationsAggregate.getPlatform(fromPlatformKey)).thenReturn(fromPlatform);

        applications.createPlatformFromExistingPlatform(platform, fromPlatformKey);

        verify(applicationsAggregate).createPlatformFromExistingPlatform(platform, fromPlatformKey);
        verify(applicationsAggregate).getPlatform(fromPlatformKey);
        verifyNoMoreInteractions(applicationsAggregate);
    }

    @Test(expected = ForbiddenOperationException.class)
    public void should_not_allow_non_prod_user_to_update_production_platform() {
        when(userContext.getCurrentUser()).thenReturn(nonProdUser);

        PlatformKey existingPlatformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();
        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(existingPlatformKey.getName())
                .withApplicationName(existingPlatformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData existingPlatform = builder1.build();

        when(applicationsAggregate.getPlatform(existingPlatformKey)).thenReturn(Optional.of(existingPlatform));

        PlatformData updatePlatform = null;
        try {
            PlatformData.IBuilder builder2 = PlatformData.withPlatformName(existingPlatformKey.getName())
                    .withApplicationName(existingPlatformKey.getApplicationName())
                    .withApplicationVersion("1.0.0.0")
                    .withModules(Sets.newHashSet())
                    .withVersion(1L);

            //Clearly show we dont upgrade to production platform
            updatePlatform = builder2.build();
            applications.updatePlatform(updatePlatform, true);
        } catch(ForbiddenOperationException e){
            verify(applicationsAggregate, never()).updatePlatform(updatePlatform, true);
            throw e;
        }

    }

    @Test(expected = ForbiddenOperationException.class)
    public void should_not_allow_non_prod_user_to_update_platform_to_production_platform(){
        when(userContext.getCurrentUser()).thenReturn(nonProdUser);

        //The existing platform is not production
        PlatformKey existingPlatformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();
        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(existingPlatformKey.getName())
                .withApplicationName(existingPlatformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        PlatformData existingPlatform = builder1.build();
        PlatformData updatePlatform = null;
        try {
            PlatformData.IBuilder builder2 = PlatformData.withPlatformName(existingPlatformKey.getName())
                    .withApplicationName(existingPlatformKey.getApplicationName())
                    .withApplicationVersion("1.0.0.0")
                    .withModules(Sets.newHashSet())
                    .withVersion(1L)
                    .isProduction();

            //Clearly show we want to upgrade to production platform
            updatePlatform = builder2.build();
            when(applicationsAggregate.getPlatform(existingPlatformKey)).thenReturn(Optional.of(existingPlatform));
            applications.updatePlatform(updatePlatform, true);
        } catch(ForbiddenOperationException e){
            verify(applicationsAggregate, never()).updatePlatform(updatePlatform, true);
            throw e;
        }
    }

    @Test
    public void should_allow_prod_user_to_update_production_platform(){
        when(userContext.getCurrentUser()).thenReturn(prodUser);
    }

    @Test
    public void should_allow_prod_user_to_update_platform_to_production_platform(){
        when(userContext.getCurrentUser()).thenReturn(prodUser);
    }

    @Test(expected = ForbiddenOperationException.class)
    public void should_not_allow_non_prod_user_to_create_or_update_production_properties(){
        when(userContext.getCurrentUser()).thenReturn(nonProdUser);

        //Existing platform is a production one
        PlatformKey existingPlatformKey = PlatformKey.withName("pltfm")
                .withApplicationName("app")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(existingPlatformKey.getName())
                .withApplicationName(existingPlatformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        when(applicationsAggregate.getPlatform(existingPlatformKey)).thenReturn(Optional.of(builder1.build()));

        try {
            //We use null instead of properties because properties values just don't matter
            applications.createOrUpdatePropertiesInPlatform(existingPlatformKey, "some_path", null, 1L, comment);
        } catch(ForbiddenOperationException e){
            verify(applicationsAggregate, never()).createOrUpdatePropertiesInPlatform(existingPlatformKey, "some_path", null, 1L, comment);
            throw e;
        }

    }

    @Test
    public void should_allow_production_user_to_create_or_update_production_properties(){
        when(userContext.getCurrentUser()).thenReturn(prodUser);
    }



}
