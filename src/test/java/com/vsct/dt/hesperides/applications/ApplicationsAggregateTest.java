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

package com.vsct.dt.hesperides.applications;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.EventStoreMock;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import com.vsct.dt.hesperides.resources.Properties;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;
import com.vsct.dt.hesperides.templating.platform.KeyValueValorisationData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;
import com.vsct.dt.hesperides.util.converter.impl.DefaultPropertiesConverter;
import com.vsct.dt.hesperides.util.converter.PropertiesConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william on 04/09/2014.
 */
public class ApplicationsAggregateTest {
    private static final PropertiesConverter PROPERTIES_CONVERTER = new DefaultPropertiesConverter();

    EventBus         eventBus        = new EventBus();
    EventStoreMock   eventStoreMock  = new EventStoreMock();
    SnapshotRegistry snapshotRegistry = mock(SnapshotRegistry.class);

    ApplicationsAggregate applications;

    private final String comment = "Test comment";

    @Before
    public void setUp() throws Exception {
        applications = new ApplicationsAggregate(eventBus, eventStoreMock, snapshotRegistry);
        eventStoreMock.reset();
    }

    @After
    public void checkUnwantedEvents() {
        eventStoreMock.verifyUnexpectedEvents();
    }

    /**
     * Platform creation
     */

    @Test
    public void should_create_platform_if_not_already_existing_and_provide_id_to_every_module_that_does_not_have_one() {
        eventStoreMock.ignoreEvents();

        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("module1")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module2 = ApplicationModuleData
                .withApplicationName("module2")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module3 = ApplicationModuleData
                .withApplicationName("module3")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(1)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module4 = ApplicationModuleData
                .withApplicationName("module4")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module5 = ApplicationModuleData
                .withApplicationName("module5")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        PlatformKey platformKey = PlatformKey.withName("a_pltfm")
                .withApplicationName("an_app")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1, module2, module3, module4, module5))
                .withVersion(1L)
                .isProduction();

        PlatformData pltfm = applications.createPlatform(builder1.build());

        assertThat(pltfm.getPlatformName()).isEqualTo("a_pltfm");
        assertThat(pltfm.getApplicationName()).isEqualTo("an_app");
        assertThat(pltfm.getApplicationVersion()).isEqualTo("app_version");
        assertThat(pltfm.getVersionID()).isEqualTo(1L);

        Set<ApplicationModuleData> modules = pltfm.getModules();
        assertThat(modules.size()).isEqualTo(5);

        //Id generation should have created ids 3,4,5
        Set<Integer> ids = modules.stream().map(module -> module.getId()).collect(Collectors.toSet());
        assertThat(ids).isEqualTo(Sets.newHashSet(1, 2, 3, 4, 5));
    }

    @Test
    public void should_create_platform_from_an_existing_one (){
        eventStoreMock.ignoreEvents();

        // Modules
        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("module1")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module2 = ApplicationModuleData
                .withApplicationName("module2")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        PlatformKey platformKey = PlatformKey.withName("myPlatform")
                .withApplicationName("myApp")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1, module2))
                .withVersion(1L)
                .isProduction();

        PlatformData fromPlatform = applications.createPlatform(builder1.build());

        // Properties ( only the keyValues ones ! )
        Set<KeyValueValorisationData> keyValueProps = Sets.newHashSet();

        keyValueProps.add( new KeyValueValorisationData("myProp1", "valOfMyProp1"));
        keyValueProps.add( new KeyValueValorisationData("myProp2", "valOfMyProp2"));
        keyValueProps.add( new KeyValueValorisationData("myProp3", "valOfMyProp3"));

        Set<KeyValueValorisationData> keyValueProps2 = Sets.newHashSet();

        keyValueProps2.add( new KeyValueValorisationData("hisProp1", "valOfHisProp1"));
        keyValueProps2.add( new KeyValueValorisationData("hisProp2", "valOfHisProp2"));
        keyValueProps2.add( new KeyValueValorisationData("hisProp3", "valOfHisProp3"));

        applications.createOrUpdatePropertiesInPlatform(fromPlatform.getKey(), "#SOME_PATH", new PropertiesData(keyValueProps, Sets.newHashSet()), 1, comment);
        applications.createOrUpdatePropertiesInPlatform(fromPlatform.getKey(), "#SOME_OTHER_PATH", new PropertiesData(keyValueProps2, Sets.newHashSet()), 2, comment);

        PlatformData platformToBeCreated = PlatformData.withPlatformName("myOtherPlatform")
                .withApplicationName("myApp")
                .withApplicationVersion("1.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction().build();

        PlatformData createdFromPlatform = applications.createPlatformFromExistingPlatform(platformToBeCreated, fromPlatform.getKey());

        // Check platform
        assertThat(createdFromPlatform).isNotNull();
        assertThat(createdFromPlatform.getApplicationName()).isEqualTo("myApp");
        assertThat(createdFromPlatform.getApplicationVersion()).isEqualTo("1.0.0");
        assertThat(createdFromPlatform.getVersionID()).isEqualTo(fromPlatform.getVersionID());

        // Check modules
        assertThat(createdFromPlatform.getModules().size()).isEqualTo(fromPlatform.getModules().size());
        Set<Integer> createdIds = fromPlatform.getModules().stream().map( module -> module.getId()).collect(Collectors.toSet());
        assertThat(createdIds).isEqualTo(Sets.newHashSet(1, 2));

        // Check properties
        assertThat(applications.getProperties(createdFromPlatform.getKey(), "#SOME_PATH")).isNotNull();
        assertThat(applications.getProperties(createdFromPlatform.getKey(), "#SOME_OTHER_PATH").getKeyValueProperties().size()).isEqualTo(keyValueProps2.size());

    }

    @Test
    public void should_fire_platform_created_event_when_a_platform_is_created(){

    }

    @Test
    public void should_not_create_platform_if_it_already_exists(){

    }

    /**
     * Platform update
     */
    @Test
    public void should_update_platform_without_copying_properties_when_path_not_changed_and_no_copy_is_asked(){

    }

    @Test
    public void should_update_platform_by_providing_module_id_to_new_modules_only(){
        eventStoreMock.ignoreEvents();

        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("module1")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(1)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module2 = ApplicationModuleData
                .withApplicationName("module2")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module3 = ApplicationModuleData
                .withApplicationName("module3")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(3)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module4 = ApplicationModuleData
                .withApplicationName("module4")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module5 = ApplicationModuleData
                .withApplicationName("module5")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(4)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module6 = ApplicationModuleData
                .withApplicationName("module6")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        PlatformKey platformKey = PlatformKey.withName("a_pltfm")
                .withApplicationName("an_app")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1, module2, module3))
                .withVersion(1L)
                .isProduction();

        PlatformData pltfm = applications.createPlatform(builder1.build());

        /*
        Updating the platfm, we remove module3 and add module 4, 5 and 6
        WE SHOULD NOT REUSE id 3
        So ids given should then be 1,2,4,5,6
         */
        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1, module2, module4, module5, module6))
                .withVersion(1L)
                .isProduction();

        PlatformData updatedPltfm = applications.updatePlatform(builder2.build(), false);

        assertThat(updatedPltfm.getPlatformName()).isEqualTo("a_pltfm");
        assertThat(updatedPltfm.getApplicationName()).isEqualTo("an_app");
        assertThat(updatedPltfm.getApplicationVersion()).isEqualTo("app_version");
        assertThat(updatedPltfm.getVersionID()).isEqualTo(2L);

        Set<ApplicationModuleData> modules = updatedPltfm.getModules();
        assertThat(modules.size()).isEqualTo(5);

        //Id generation should have created ids 3,4,5
        Set<Integer> ids = modules.stream().map(module -> module.getId()).collect(Collectors.toSet());
        assertThat(ids).isEqualTo(Sets.newHashSet(1,2,4,5,6));
    }

    @Test
    public void should_update_platform_copying_properties_for_updated_modules_if_asked(){

    }

    //Add tests for version id



    @Test
    public void should_delete_plateform_and_related_properties(){
        eventStoreMock.ignoreEvents();

        PlatformKey key = new PlatformKey("FOO", "REL1");

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        applications.createPlatform(builder2.build());
        applications.createOrUpdatePropertiesInPlatform(key, "#SOME_PATH", new PropertiesData(Sets.newHashSet(), Sets.newHashSet()), 1, comment);
        applications.createOrUpdatePropertiesInPlatform(key, "#SOME_OTHER_PATH", new PropertiesData(Sets.newHashSet(), Sets.newHashSet()), 2, comment);

        assertThat(applications.getPlatform(key).isPresent()).isTrue();
        assertThat(applications.getProperties(key, "#SOME_PATH")).isNotNull();
        assertThat(applications.getProperties(key, "#SOME_OTHER_PATH")).isNotNull();

        applications.delete(key);
        assertThat(applications.getPlatform(key).isPresent()).isFalse();
        assertThat(applications.getProperties(key, "#SOME_PATH").getIterableProperties().size()).isEqualTo(0);
        assertThat(applications.getProperties(key, "#SOME_PATH").getKeyValueProperties().size()).isEqualTo(0);
        assertThat(applications.getProperties(key, "#SOME_OTHER_PATH").getIterableProperties().size()).isEqualTo(0);
        assertThat(applications.getProperties(key, "#SOME_OTHER_PATH").getKeyValueProperties().size()).isEqualTo(0);
    }

    @Test
    public void deleting_platform_should_fire_an_event(){
        PlatformKey key = new PlatformKey("FOO", "REL1");

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        applications.createPlatform(builder2.build());
        eventStoreMock.reset();

        applications.delete(key);

        eventStoreMock.checkSavedTheEventOnStream("platform-FOO-REL1", new PlatformDeletedEvent(key.getApplicationName(), key.getName()));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_exception_when_trying_to_delete_unknown_paltform(){
        eventStoreMock.ignoreEvents();
        PlatformKey key = new PlatformKey("FOO", "REL1");
        applications.delete(key);
    }

    @Test
    public void snapshoting_platform_should_call_snapshot_service(){
        eventStoreMock.ignoreEvents();
        PlatformKey key = new PlatformKey("FOO", "REL1");
        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("module1")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(1)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module2 = ApplicationModuleData
                .withApplicationName("module2")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module3 = ApplicationModuleData
                .withApplicationName("module3")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet(module1, module2, module3))
                .withVersion(1L);

        applications.createPlatform(builder2.build());

        //Create some properties
        KeyValueValorisationData p1 = new KeyValueValorisationData("prop1", "value1");
        KeyValueValorisationData p2 = new KeyValueValorisationData("prop2", "value2");
        KeyValueValorisationData p3 = new KeyValueValorisationData("prop3", "value3");
        KeyValueValorisationData p4 = new KeyValueValorisationData("prop4", "value4");
        KeyValueValorisationData p5 = new KeyValueValorisationData("prop5", "value5");
        KeyValueValorisationData p6 = new KeyValueValorisationData("prop6", "value6");
        PropertiesData properties1 = new PropertiesData(Sets.newHashSet(p1, p2), Sets.newHashSet());
        PropertiesData properties2 = new PropertiesData(Sets.newHashSet(p3, p4), Sets.newHashSet());
        PropertiesData properties3 = new PropertiesData(Sets.newHashSet(p5, p6), Sets.newHashSet());
        applications.createOrUpdatePropertiesInPlatform(key, module1.getPropertiesPath(), properties1, 1, comment);
        applications.createOrUpdatePropertiesInPlatform(key, module2.getPropertiesPath(), properties2, 2, comment);
        applications.createOrUpdatePropertiesInPlatform(key, "#SOME_PATH_FROM_OLDER_MODULE", properties3, 3, comment);
        //We add no properties for module3, something like this can happen

        long timestamp = applications.takeSnapshot(key);

        Map<String, PropertiesData> properties = new HashMap<>();
        properties.put(module1.getPropertiesPath(), properties1);
        properties.put(module2.getPropertiesPath(), properties2);
        PlatformSnapshot snapshot = new PlatformSnapshot(builder2.build(), properties);

        PlatformSnapshotKey platformSnapshotKey = new PlatformSnapshotKey(timestamp, key);
        verify(snapshotRegistry).createSnapshot(platformSnapshotKey, snapshot);
    }

    @Test
    public void snapshoting_platform_should_fire_an_event(){
        PlatformKey key = new PlatformKey("FOO", "REL1");
        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("module1")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(1)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module2 = ApplicationModuleData
                .withApplicationName("module2")
                .withVersion("version")
                .withPath("#FOO#WAS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet(module1, module2))
                .withVersion(1L);

        applications.createPlatform(builder2.build());
        eventStoreMock.reset();

        //Create some properties
        KeyValueValorisationData p1 = new KeyValueValorisationData("prop1", "value1");
        KeyValueValorisationData p2 = new KeyValueValorisationData("prop2", "value2");
        KeyValueValorisationData p3 = new KeyValueValorisationData("prop3", "value3");
        KeyValueValorisationData p4 = new KeyValueValorisationData("prop4", "value4");
        KeyValueValorisationData p5 = new KeyValueValorisationData("prop5", "value5");
        KeyValueValorisationData p6 = new KeyValueValorisationData("prop6", "value6");
        PropertiesData properties1 = new PropertiesData(Sets.newHashSet(p1, p2), Sets.newHashSet());
        PropertiesData properties2 = new PropertiesData(Sets.newHashSet(p3, p4), Sets.newHashSet());
        PropertiesData properties3 = new PropertiesData(Sets.newHashSet(p5, p6), Sets.newHashSet());
        applications.createOrUpdatePropertiesInPlatform(key, module1.getPropertiesPath(), properties1, 1, comment);
        eventStoreMock.reset();
        applications.createOrUpdatePropertiesInPlatform(key, module2.getPropertiesPath(), properties2, 2, comment);
        eventStoreMock.reset();
        applications.createOrUpdatePropertiesInPlatform(key, "#SOME_PATH_FROM_OLDER_MODULE", properties3, 3, comment);
        eventStoreMock.reset();

        long timestamp = applications.takeSnapshot(key);

        eventStoreMock.checkSavedTheEventOnStream("platform-FOO-REL1", new PlatformSnapshotEvent(timestamp, "FOO", "REL1"));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_exception_when_trying_to_snapshot_unknown_platform(){
        eventStoreMock.ignoreEvents();
        PlatformKey key = new PlatformKey("FOO", "REL1");
        applications.takeSnapshot(key);
    }

    @Test
    public void should_restore_snapshot_with_an_updated_version_id_when_platform_exists(){
        eventStoreMock.ignoreEvents();
        PlatformKey key = new PlatformKey("FOO", "REL1");

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        PlatformData platform = builder2.build();

        applications.createPlatform(platform);

        //Platform is created, id is 1

        //Simulate a response from the snapshot registry
        KeyValueValorisationData p1 = new KeyValueValorisationData("prop1", "value1");
        KeyValueValorisationData p2 = new KeyValueValorisationData("prop2", "value2");
        KeyValueValorisationData p3 = new KeyValueValorisationData("prop3", "value3");
        KeyValueValorisationData p4 = new KeyValueValorisationData("prop4", "value4");
        KeyValueValorisationData p5 = new KeyValueValorisationData("prop5", "value5");
        KeyValueValorisationData p6 = new KeyValueValorisationData("prop6", "value6");
        PropertiesData properties1 = new PropertiesData(Sets.newHashSet(p1, p2), Sets.newHashSet());
        PropertiesData properties2 = new PropertiesData(Sets.newHashSet(p3, p4), Sets.newHashSet());
        PropertiesData properties3 = new PropertiesData(Sets.newHashSet(p5, p6), Sets.newHashSet());
        Map<String, PropertiesData> pathToProperties = new HashMap<>();
        pathToProperties.put("#PATH1", properties1);
        pathToProperties.put("#PATH2", properties2);
        pathToProperties.put("#PATH3", properties3);

        PlatformSnapshotKey snapshotKey = new PlatformSnapshotKey(123456789, key);
        PlatformSnapshot snapshot = new PlatformSnapshot(platform, pathToProperties);
        when(snapshotRegistry.getSnapshot(snapshotKey, PlatformSnapshot.class)).thenReturn(Optional.of(snapshot));

        PlatformData result = applications.restoreSnapshot(key, 123456789);

        PlatformData stored = applications.getPlatform(key).get();

        assertThat(result).isEqualTo(stored);
        assertThat(stored.getVersionID()).isEqualTo(2);

        assertThat(applications.getProperties(key, "#PATH1")).isEqualTo(properties1);
        assertThat(applications.getProperties(key, "#PATH2")).isEqualTo(properties2);
        assertThat(applications.getProperties(key, "#PATH3")).isEqualTo(properties3);

    }

    @Test
    public void should_restore_snapshot_with_version_id_at_1_when_platform_was_deleted(){
        eventStoreMock.ignoreEvents();
        PlatformKey key = new PlatformKey("FOO", "REL1");

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        applications.createPlatform(builder2.build());
        applications.delete(key);

        //Platform is created, id is 1

        //Simulate a response from the snapshot registry
        KeyValueValorisationData p1 = new KeyValueValorisationData("prop1", "value1");
        KeyValueValorisationData p2 = new KeyValueValorisationData("prop2", "value2");
        KeyValueValorisationData p3 = new KeyValueValorisationData("prop3", "value3");
        KeyValueValorisationData p4 = new KeyValueValorisationData("prop4", "value4");
        KeyValueValorisationData p5 = new KeyValueValorisationData("prop5", "value5");
        KeyValueValorisationData p6 = new KeyValueValorisationData("prop6", "value6");
        PropertiesData properties1 = new PropertiesData(Sets.newHashSet(p1, p2), Sets.newHashSet());
        PropertiesData properties2 = new PropertiesData(Sets.newHashSet(p3, p4), Sets.newHashSet());
        PropertiesData properties3 = new PropertiesData(Sets.newHashSet(p5, p6), Sets.newHashSet());
        Map<String, PropertiesData> pathToProperties = new HashMap<>();
        pathToProperties.put("#PATH1", properties1);
        pathToProperties.put("#PATH2", properties2);
        pathToProperties.put("#PATH3", properties3);

        PlatformSnapshotKey snapshotKey = new PlatformSnapshotKey(123456789, key);
        PlatformSnapshot snapshot = new PlatformSnapshot(builder2.build(), pathToProperties);
        when(snapshotRegistry.getSnapshot(snapshotKey, PlatformSnapshot.class)).thenReturn(Optional.of(snapshot));

        applications.restoreSnapshot(key, 123456789);

        PlatformData stored = applications.getPlatform(key).get();

        assertThat(stored.getVersionID()).isEqualTo(1);
    }

    @Test
    public void should_fire_an_event_when_restoring_a_snapshot(){
        PlatformKey key = new PlatformKey("FOO", "REL1");

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        applications.createPlatform(builder2.build());
        eventStoreMock.reset();

        //Platform is created, id is 1

        //Simulate a response from the snapshot registry
        KeyValueValorisationData p1 = new KeyValueValorisationData("prop1", "value1");
        KeyValueValorisationData p2 = new KeyValueValorisationData("prop2", "value2");
        KeyValueValorisationData p3 = new KeyValueValorisationData("prop3", "value3");
        KeyValueValorisationData p4 = new KeyValueValorisationData("prop4", "value4");
        KeyValueValorisationData p5 = new KeyValueValorisationData("prop5", "value5");
        KeyValueValorisationData p6 = new KeyValueValorisationData("prop6", "value6");
        PropertiesData properties1 = new PropertiesData(Sets.newHashSet(p1, p2), Sets.newHashSet());
        PropertiesData properties2 = new PropertiesData(Sets.newHashSet(p3, p4), Sets.newHashSet());
        PropertiesData properties3 = new PropertiesData(Sets.newHashSet(p5, p6), Sets.newHashSet());
        Map<String, PropertiesData> pathToProperties = new HashMap<>();
        pathToProperties.put("#PATH1", properties1);
        pathToProperties.put("#PATH2", properties2);
        pathToProperties.put("#PATH3", properties3);

        PlatformSnapshotKey snapshotKey = new PlatformSnapshotKey(123456789, key);
        PlatformSnapshot snapshot = new PlatformSnapshot(builder2.build(), pathToProperties);
        when(snapshotRegistry.getSnapshot(snapshotKey, PlatformSnapshot.class)).thenReturn(Optional.of(snapshot));

        applications.restoreSnapshot(key, 123456789);

        eventStoreMock.checkSavedTheEventOnStream("platform-FOO-REL1", new PlatformSnapshotRestoreEvent(123456789, snapshot));
    }

    //This will not test a lot the structure of the instance properties itself, since it is already tested in PropertiesTest
    //We will just check the presence of one property
    @Test
    public void get_instance_model_should_return_the_instance_model_for_a_given_platform_and_properties_path(){
        eventStoreMock.ignoreEvents();

        PlatformKey key = new PlatformKey("FOO", "REL1");

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        applications.createPlatform(builder2.build());

        KeyValueValorisationData p1 = new KeyValueValorisationData("prop1", "{{instance.property}}");
        PropertiesData properties = new PropertiesData(Sets.newHashSet(p1), Sets.newHashSet());

        applications.createOrUpdatePropertiesInPlatform(key, "#some#path", properties, 1, comment);

        //Get the isntance model
        InstanceModel model = applications.getInstanceModel(key, "#some#path");

        assertThat(model.getKeys().size()).isEqualTo(1);
        assertThat(model.getKeys().iterator().next().getName()).isEqualTo("instance.property");
    }

    @Test(expected = MissingResourceException.class)
    public void get_instance_model_should_throw_missing_ressource_exception_when_platform_is_missing(){
        PlatformKey key = new PlatformKey("FOO", "REL1");
        applications.getInstanceModel(key, "#some#path");
    }

    //This will not test a lot the structure of the instance properties itself, since it is already tested in PropertiesTest
    //We will just check the presence of one property coming from the global scope
    @Test
    public void get_instance_model_should_also_include_platform_global_properties(){
        eventStoreMock.ignoreEvents();

        PlatformKey key = new PlatformKey("FOO", "REL1");

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(key.getName())
                .withApplicationName(key.getApplicationName())
                .withApplicationVersion("version")
                .withModules(Sets.newHashSet())
                .withVersion(1L);

        applications.createPlatform(builder2.build());

        KeyValueValorisation p1 = new KeyValueValorisation("prop1", "{{instance.property.from.global}}");
        Properties properties = new Properties(Sets.newHashSet(p1), Sets.newHashSet());

        applications.createOrUpdatePropertiesInPlatform(key, "#", PROPERTIES_CONVERTER.toPropertiesData(properties), 1, comment);

        //Get the isntance model
        InstanceModel model = applications.getInstanceModel(key, "#some#path");

        assertThat(model.getKeys().size()).isEqualTo(1);
        assertThat(model.getKeys().iterator().next().getName()).isEqualTo("instance.property.from.global");
    }

    @Test
    public void unit_test_getApplicationFromSelector() {
        eventStoreMock.ignoreEvents();

        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("demoKatana")
                .withVersion("1.0.0")
                .withPath("#FOO#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .setWorkingcopy(true)
                .build();

        ApplicationModuleData module2 = ApplicationModuleData
                .withApplicationName("demoKatana")
                .withVersion("1.0.0")
                .withPath("#FOO#WAS")
                .withId(1)
                .withInstances(Sets.newHashSet())
                .setWorkingcopy(false)
                .build();

        ApplicationModuleData module3 = ApplicationModuleData
                .withApplicationName("demoKatana")
                .withVersion("1.0.1")
                .withPath("#FOO#WAS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        ApplicationModuleData module4 = ApplicationModuleData
                .withApplicationName("demoSabre")
                .withVersion("1.0.0")
                .withPath("#FOO#WAS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();


        PlatformKey platformKey1 = PlatformKey.withName("usn1")
                .withApplicationName("ktn")
                .build();

        PlatformKey platformKey2 = PlatformKey.withName("usn2")
                .withApplicationName("ktn")
                .build();

        PlatformKey platformKey3 = PlatformKey.withName("usn3")
                .withApplicationName("test")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey1.getName())
                .withApplicationName(platformKey1.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1, module2, module3))
                .withVersion(1L)
                .isProduction();
        PlatformData.IBuilder builder2 = PlatformData.withPlatformName(platformKey2.getName())
                .withApplicationName(platformKey2.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1, module2))
                .withVersion(1L)
                .isProduction();
        PlatformData.IBuilder builder3 = PlatformData.withPlatformName(platformKey3.getName())
                .withApplicationName(platformKey3.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module3, module4))
                .withVersion(1L)
                .isProduction();


        PlatformData pltfm1 = applications.createPlatform(builder1.build());
        PlatformData pltfm2 = applications.createPlatform(builder2.build());
        PlatformData pltfm3 = applications.createPlatform(builder3.build());


        Collection<PlatformData> test0 = applications.getApplicationsFromSelector(data -> true);
        assertThat(test0.size()).isEqualTo(3);

        Collection<PlatformData> test1 = applications.getApplicationsFromSelector(
                data -> data.getModules().stream().anyMatch(
                        elem -> elem.getName().equals("demoKatana") && elem.getVersion().equals("1.0.0") && elem.isWorkingCopy()));
        assertThat(test1.size()).isEqualTo(2);
        assertThat(test1).isEqualTo(Arrays.asList(pltfm2, pltfm1));

        Collection<PlatformData> test2 = applications.getApplicationsFromSelector(
                data -> data.getModules().stream().anyMatch(
                        elem -> elem.getName().equals("demoKatana") && elem.getVersion().equals("1.0.0") && !elem.isWorkingCopy()));
        assertThat(test2.size()).isEqualTo(2);
        assertThat(test2).isEqualTo(Arrays.asList(pltfm2, pltfm1));

        Collection<PlatformData> test3 = applications.getApplicationsFromSelector(
                data -> data.getModules().stream().anyMatch(
                        elem -> elem.getName().equals("demoSabre")));
        assertThat(test3).isEqualTo(Arrays.asList(pltfm3));

        Collection<PlatformData> test4 = applications.getApplicationsFromSelector(
                data -> data.getModules().stream().anyMatch(
                        elem -> elem.getName().equals("demoSabredsqd")));
        assertThat(test4).isEqualTo(Arrays.asList());

        Collection<PlatformData> test5 = applications.getApplicationsFromSelector(
                data -> data.getApplicationName().equals("test"));
        assertThat(test5).isEqualTo(Arrays.asList(pltfm3));
    }


}
