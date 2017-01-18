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

package com.vsct.dt.hesperides.templating;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.applications.InstanceModel;
import com.vsct.dt.hesperides.resources.IterableValorisation;
import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import com.vsct.dt.hesperides.resources.Properties;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import com.vsct.dt.hesperides.templating.platform.IterableValorisationData;
import com.vsct.dt.hesperides.templating.platform.KeyValueValorisationData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.fest.assertions.api.Assertions.assertThat;


public class PropertyTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Before
    public void setup(){
    }

    @Test
    public void shouldSerializeToJSON() throws IOException {
        /* Create the KeyValueProperty */
        KeyValueValorisation kvp = new KeyValueValorisation("name", "value");

        /* Create the iterable property */
        IterableValorisation.IterableValorisationItem itemIterable2 = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name3", "value")));
        IterableValorisation iterableValorisation2 = new IterableValorisation("iterable2", Lists.newArrayList(itemIterable2));

        IterableValorisation.IterableValorisationItem item = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name2", "value"), iterableValorisation2));
        IterableValorisation iterableValorisation = new IterableValorisation("iterable", Lists.newArrayList(item));

        Properties valorisations = new Properties(Sets.newHashSet(kvp), Sets.newHashSet(iterableValorisation));

        assertThat(MAPPER.writeValueAsString(valorisations)).isEqualTo(flattenJSON("fixtures/business/properties.json"));
    }

    @Test
    public void shouldDeserializeFromJSON() throws IOException {
        /* Create the KeyValueProperty */
        KeyValueValorisation kvp = new KeyValueValorisation("name", "value");

        /* Create the iterable property */
        IterableValorisation.IterableValorisationItem itemIterable2 = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name3", "value")));
        IterableValorisation iterableValorisation2 = new IterableValorisation("iterable2", Lists.newArrayList(itemIterable2));

        IterableValorisation.IterableValorisationItem item = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name2", "value"), iterableValorisation2));
        IterableValorisation iterableValorisation = new IterableValorisation("iterable", Lists.newArrayList(item));

        Properties valorisations = new Properties(Sets.newHashSet(kvp), Sets.newHashSet(iterableValorisation));

        assertThat(DeepEquals.deepEquals(MAPPER.readValue(fixture("fixtures/business/properties.json"), Properties.class), valorisations)).isTrue();
    }

    @Test
    public void shouldProduceMustacheScopeWithEmptyInstanceContext() {
        /*

        For a property like this :

        {
            "key_value_properties": [
                {
                    "name": "kvp1",
                    "value": "value_kvp1"
                },
                {
                    "name": "kvp2",
                    "value": "value_kvp2"
                }
            ],
            "iterable_properties": [
                {
                    "name": "iterable",
                    "iterable_valorisations": [
                        {
                            "title": "blockOfProperties",
                            "values": [
                                {
                                    "name": "field1",
                                    "value": "value_field11"
                                },
                                {
                                    "name": "field2_iterable",
                                    "iterable_valorisations": [
                                        "title": "blockOfProperties",
                                        "values": [
                                            {
                                                "name": "field21A",
                                                "value": "value_field21A"
                                            },
                                            {
                                                "name": "field21B",
                                                "value": "value_field21B"
                                            }
                                        ]
                                    ]
                                }
                            ]
                        },
                        {
                            "title": "blockOfProperties2",
                            "values": [
                                {
                                    "name": "field1",
                                    "value": "value_field12"
                                },
                                {
                                    "name": "field2_iterable",
                                    "iterable_valorisations": [
                                        "title": "blockOfProperties",
                                        "values": [
                                            {
                                                "name": "field22A",
                                                "value": "value_field22A"
                                            },
                                            {
                                                "name": "field22B",
                                                "value": "value_field22B"
                                            }
                                        ]
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }

        the mustache scope should be a Map<String, Object> containing

        "kvp1" -> "value_kvp1",
        "kvp2" -> "value_kvp2",
        "iterable" -> [
                       {
                        "field1" -> "value_field1A"
                        "field2_iterable" -> [
                            "field21A" -> "value_field21A",
                            "field21B" -> "value_field21B"
                        ]
                       },
                       {
                        "field1" -> "value_field1B"
                        "field2_iterable"  -> [
                            "field22A" -> "value_field22A",
                            "field22B" -> "value_field22B"
                        ]
                       }
                      ]

        Speaking of type,
        a set of keyValueProperties will produce Map<String, String>
        a set of iterableProperties will produce Map<String, List<Map<String, Object>>

        The full mustache scope is the union of thoose 2 maps

        /* Create the KeyValueProperties */
        KeyValueValorisationData kvp1 = new KeyValueValorisationData("kvp1", "value_kvp1");
        KeyValueValorisationData kvp2 = new KeyValueValorisationData("kvp2", "value_kvp2");

        /* Create the iterable property */
        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value_field1A"), new KeyValueValorisationData("field2", "value_field2A")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value_field1B"), new KeyValueValorisationData("field2", "value_field2B")));

        IterableValorisationData iterableValorisation = new IterableValorisationData("iterable", Lists.newArrayList(item1, item2));

        PropertiesData valorisations = new PropertiesData(Sets.newHashSet(kvp1, kvp2), Sets.newHashSet(iterableValorisation));

        Map<String, Object> scope = valorisations.toMustacheScope(Sets.newHashSet(), null);

        assertThat(scope.size()).isEqualTo(3);
        assertThat(scope.containsKey("kvp1")).isTrue();
        assertThat(scope.containsKey("kvp2")).isTrue();
        assertThat(scope.containsKey("iterable")).isTrue();

        assertThat(scope.get("kvp1")).isEqualTo("value_kvp1");
        assertThat(scope.get("kvp2")).isEqualTo("value_kvp2");

        List<Map<String, Object>> iScope = (List<Map<String, Object>>) scope.get("iterable");
        assertThat(iScope.size()).isEqualTo(2);

        Map<String, Object> bopScope1 = iScope.get(0);
        Map<String, Object> bopScope2 = iScope.get(1);

        assertThat(bopScope1.size()).isEqualTo(2);
        assertThat(bopScope1.containsKey("field1")).isTrue();
        assertThat(bopScope1.containsKey("field2")).isTrue();
        assertThat(bopScope1.get("field1")).isEqualTo("value_field1A");
        assertThat(bopScope1.get("field2")).isEqualTo("value_field2A");

        assertThat(bopScope2.size()).isEqualTo(2);
        assertThat(bopScope2.containsKey("field1")).isTrue();
        assertThat(bopScope2.containsKey("field2")).isTrue();
        assertThat(bopScope2.get("field1")).isEqualTo("value_field1B");
        assertThat(bopScope2.get("field2")).isEqualTo("value_field2B");

    }

    @Test
    public void shouldConstructMustacheScopeWithEvaluationOfInstanceContext() {
        /* Use the same structure but all values will use the same context key (everything related to context is somewhere else
        Here we only matter for values replacement everytwhere
         */

        /* Create the KeyValueProperties */
        KeyValueValorisationData kvp1 = new KeyValueValorisationData("kvp1", "{{replacement}}/{{replacement}}");
        KeyValueValorisationData kvp2 = new KeyValueValorisationData("kvp2", "{{replacement}}/{{replacement}}");

        /* Create the iterable property */
        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "{{replacement}}"), new KeyValueValorisationData("field2", "{{replacement}}")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "{{replacement}}"), new KeyValueValorisationData("field2", "{{replacement}}")));

        IterableValorisationData iterableValorisation = new IterableValorisationData("iterable", Lists.newArrayList(item1, item2));

        PropertiesData valorisations = new PropertiesData(Sets.newHashSet(kvp1, kvp2), Sets.newHashSet(iterableValorisation));

        Map<String, Object> scope = valorisations.toMustacheScope(Sets.newHashSet(new KeyValueValorisationData("replacement", "fromInstance")), null);

        assertThat(scope.size()).isEqualTo(3);
        assertThat(scope.containsKey("kvp1")).isTrue();
        assertThat(scope.containsKey("kvp2")).isTrue();
        assertThat(scope.containsKey("iterable")).isTrue();

        assertThat(scope.get("kvp1")).isEqualTo("fromInstance/fromInstance");
        assertThat(scope.get("kvp2")).isEqualTo("fromInstance/fromInstance");

        List<Map<String, Object>> iScope = (List<Map<String, Object>>) scope.get("iterable");
        assertThat(iScope.size()).isEqualTo(2);

        Map<String, Object> bopScope1 = (Map<String, Object>) iScope.get(0);
        Map<String, Object> bopScope2 = (Map<String, Object>) iScope.get(1);

        assertThat(bopScope1.size()).isEqualTo(2);
        assertThat(bopScope1.containsKey("field1")).isTrue();
        assertThat(bopScope1.containsKey("field2")).isTrue();
        assertThat(bopScope1.get("field1")).isEqualTo("fromInstance");
        assertThat(bopScope1.get("field2")).isEqualTo("fromInstance");

        assertThat(bopScope2.size()).isEqualTo(2);
        assertThat(bopScope2.containsKey("field1")).isTrue();
        assertThat(bopScope2.containsKey("field2")).isTrue();
        assertThat(bopScope2.get("field1")).isEqualTo("fromInstance");
        assertThat(bopScope2.get("field2")).isEqualTo("fromInstance");

    }

    @Test
    public void shouldConstructMustacheScopeWithKeyValueValorisationsCallingEachOther(){

        /* Create the KeyValueProperties */
        KeyValueValorisationData kvp1 = new KeyValueValorisationData("kvp1", "something {{kvp2}} {{kvp3}}");
        KeyValueValorisationData kvp2 = new KeyValueValorisationData("kvp2", "{{replacement}}");
        KeyValueValorisationData kvp3 = new KeyValueValorisationData("kvp3", "fromKvp3");

        PropertiesData valorisations = new PropertiesData(Sets.newHashSet(kvp1, kvp2, kvp3), Sets.newHashSet());

        Map<String, Object> scope = valorisations.toMustacheScope(Sets.newHashSet(new KeyValueValorisationData("replacement", "fromInstance")), null);

        assertThat(scope.size()).isEqualTo(3);
        assertThat(scope.containsKey("kvp1")).isTrue();
        assertThat(scope.containsKey("kvp2")).isTrue();
        assertThat(scope.containsKey("kvp3")).isTrue();

        assertThat(scope.get("kvp1")).isEqualTo("something fromInstance fromKvp3");
        assertThat(scope.get("kvp2")).isEqualTo("fromInstance");
        assertThat(scope.get("kvp3")).isEqualTo("fromKvp3");
    }

    @Test
    public void shouldConstructMustacheScopeWithKeyValueValorisationsCallingEachOtherWithCircularReferences(){

        /* Create the KeyValueProperties */
        KeyValueValorisationData kvp1 = new KeyValueValorisationData("kvp1", "something {{kvp2}}");
        KeyValueValorisationData kvp2 = new KeyValueValorisationData("kvp2", "{{kvp2}}");

        PropertiesData valorisations = new PropertiesData(Sets.newHashSet(kvp1, kvp2), Sets.newHashSet());

        Map<String, Object> scope = valorisations.toMustacheScope(Sets.newHashSet(new KeyValueValorisationData("kvp2", "fromInstance")), null);

        assertThat(scope.size()).isEqualTo(2);
        assertThat(scope.containsKey("kvp1")).isTrue();
        assertThat(scope.containsKey("kvp2")).isTrue();

        assertThat(scope.get("kvp1")).isEqualTo("something fromInstance");
        assertThat(scope.get("kvp2")).isEqualTo("fromInstance");

    }

    @Test
    public void shouldConstructMustacheScopeWithInjectionFromPlatformPropertiesInKeyValueValorisations(){

        /* Create 3 valorisations, one not touche (kvp2), one totally replaced because it has the same name (platform_prop), one just partially replaced (kvp3) */
        KeyValueValorisationData kvp1 = new KeyValueValorisationData("platform_prop", "");
        KeyValueValorisationData kvp2 = new KeyValueValorisationData("kvp2", "");
        KeyValueValorisationData kvp3 = new KeyValueValorisationData("kvp3", "something {{platform_prop}}");
        PropertiesData moduleValorisations = new PropertiesData(Sets.newHashSet(kvp1, kvp2, kvp3), Sets.newHashSet());

        /* Values */
        KeyValueValorisationData platformKv = new KeyValueValorisationData("platform_prop", "PLATFORM");
        Map<String, Object> scope = moduleValorisations.toMustacheScope(Sets.newHashSet(), Sets.newHashSet(platformKv));

        assertThat(scope.size()).isEqualTo(3);
        assertThat(scope.containsKey("platform_prop")).isTrue();
        assertThat(scope.containsKey("kvp2")).isTrue();
        assertThat(scope.containsKey("kvp3")).isTrue();

        assertThat(scope.get("platform_prop")).isEqualTo("PLATFORM");
        assertThat(scope.get("kvp2")).isEqualTo("");
        assertThat(scope.get("kvp3")).isEqualTo("something PLATFORM");

    }

    @Test
    public void shouldConstructMustacheScopeInjectingKeyValueValorisationsInIterableValorisations(){

        /* Create the KeyValueProperties */
        KeyValueValorisationData kvp1 = new KeyValueValorisationData("kvp1", "{{replacement}}");

        /* Create 2 iterable valorisations */
        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "standard valorisation"), new KeyValueValorisationData("field2", "standard valorisation")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "field1B {{kvp1}}"), new KeyValueValorisationData("field2", "field2B {{kvp1}}")));

        IterableValorisationData iterableValorisation1 = new IterableValorisationData("iterable1", Lists.newArrayList(item1, item2));

        IterableValorisationData.IterableValorisationItemData item3 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "standard valorisation"), new KeyValueValorisationData("field2", "standard valorisation")));
        IterableValorisationData.IterableValorisationItemData item4 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "field1D {{kvp1}}"), new KeyValueValorisationData("field2", "field2D {{kvp1}}")));

        IterableValorisationData iterableValorisation2 = new IterableValorisationData("iterable2", Lists.newArrayList(item3, item4));

        PropertiesData valorisations = new PropertiesData(Sets.newHashSet(kvp1), Sets.newHashSet(iterableValorisation1, iterableValorisation2));

        /* Create an instance with instance level valorisation */
        Map<String, Object> scope = valorisations.toMustacheScope(Sets.newHashSet(new KeyValueValorisationData("replacement", "fromInstance")), null);

        assertThat(scope.get("kvp1")).isEqualTo("fromInstance");

        List<Map<String, Object>> iterable1 = (List<Map<String, Object>>) scope.get("iterable1");
        List<Map<String, Object>> iterable2 = (List<Map<String, Object>>) scope.get("iterable2");

        assertThat(iterable1.get(0).get("field1")).isEqualTo("standard valorisation");
        assertThat(iterable1.get(0).get("field2")).isEqualTo("standard valorisation");

        assertThat(iterable1.get(1).get("field1")).isEqualTo("field1B fromInstance");
        assertThat(iterable1.get(1).get("field2")).isEqualTo("field2B fromInstance");

        assertThat(iterable2.get(0).get("field1")).isEqualTo("standard valorisation");
        assertThat(iterable2.get(0).get("field2")).isEqualTo("standard valorisation");

        assertThat(iterable2.get(1).get("field1")).isEqualTo("field1D fromInstance");
        assertThat(iterable2.get(1).get("field2")).isEqualTo("field2D fromInstance");

    }

    @Test
    public void shouldConstructMustacheScopeWithInjectionFromPlatformPropertiesInIterableValorisations(){

        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "hello {{platform_prop}}"), new KeyValueValorisationData("field2", "bye bye {{platform_prop}}")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "hello {{platform_prop}}"), new KeyValueValorisationData("field2", "bye bye {{platform_prop}}")));

        IterableValorisationData iterableValorisation1 = new IterableValorisationData("iterable2", Lists.newArrayList(item1, item2));

        PropertiesData moduleValorisations = new PropertiesData(Sets.newHashSet(), Sets.newHashSet(iterableValorisation1));

        KeyValueValorisationData platformKv = new KeyValueValorisationData("platform_prop", "PLATFORM");
        Map<String, Object> scope = moduleValorisations.toMustacheScope(Sets.newHashSet(), Sets.newHashSet(platformKv));

        List<Map<String, Object>> iterable1 = (List<Map<String, Object>>) scope.get("iterable2");

        assertThat(iterable1.get(0).get("field1")).isEqualTo("hello PLATFORM");
        assertThat(iterable1.get(0).get("field2")).isEqualTo("bye bye PLATFORM");

        assertThat(iterable1.get(1).get("field1")).isEqualTo("hello PLATFORM");
        assertThat(iterable1.get(1).get("field2")).isEqualTo("bye bye PLATFORM");

    }

    @Test
    public void shouldConstructMustacheScopeWithGlobalValorisationsCallingEachOther(){
        KeyValueValorisationData kvp1 = new KeyValueValorisationData("some_prop", "{{platform_prop}}");
        PropertiesData moduleValorisations = new PropertiesData(Sets.newHashSet(kvp1), Sets.newHashSet());

        /* Values */
        KeyValueValorisationData platformKv1 = new KeyValueValorisationData("platform_prop", "PLATFORM {{another_platform_prop}}");
        KeyValueValorisationData platformKv2 = new KeyValueValorisationData("another_platform_prop", "INCEPTION");
        Map<String, Object> scope = moduleValorisations.toMustacheScope(Sets.newHashSet(), Sets.newHashSet(platformKv1, platformKv2));

        assertThat(scope.size()).isEqualTo(3);
        assertThat(scope.containsKey("some_prop")).isTrue();
        assertThat(scope.containsKey("platform_prop")).isTrue();
        assertThat(scope.containsKey("another_platform_prop")).isTrue();

        assertThat(scope.get("platform_prop")).isEqualTo("PLATFORM INCEPTION");
        assertThat(scope.get("another_platform_prop")).isEqualTo("INCEPTION");
        assertThat(scope.get("some_prop")).isEqualTo("PLATFORM INCEPTION");
    }

    //Only test simple properties for now

    @Test
    public void get_instance_model_should_return_empty_model_if_there_is_no_property_needed(){
        //Create the properties
        Set<KeyValueValorisationData> kvp = Sets.newHashSet();

        kvp.add(new KeyValueValorisationData("name1", "value1"));
        kvp.add(new KeyValueValorisationData("name2", "value2"));
        kvp.add(new KeyValueValorisationData("name3", "value3"));

        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value")));

        IterableValorisationData iv1 = new IterableValorisationData("iterable1", Lists.newArrayList(item1, item2));

        IterableValorisationData.IterableValorisationItemData item3 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value"), new KeyValueValorisationData("field2", "value")));
        IterableValorisationData.IterableValorisationItemData item4 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value"), new KeyValueValorisationData("field2", "value")));

        IterableValorisationData iv2 = new IterableValorisationData("iterable2", Lists.newArrayList(item3, item4));

        PropertiesData properties = new PropertiesData(kvp, Sets.newHashSet(iv1, iv2));

        //Slurp'em
        InstanceModel model = properties.generateInstanceModel(null);

        //Model should be empty
        assertThat(model.getKeys().size()).isEqualTo(0);
    }

    @Test
    public void get_instance_model_should_return_model_containing_keys_having_different_names_than_properties(){
        //Create the properties
        Set<KeyValueValorisationData> kvp = Sets.newHashSet();

        kvp.add(new KeyValueValorisationData("name1", "value {{key1}}"));
        kvp.add(new KeyValueValorisationData("name2", "value {{key2}}"));
        kvp.add(new KeyValueValorisationData("name3", "value {{key3}}"));

        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key4}}")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key5}}")));

        IterableValorisationData iv1 = new IterableValorisationData("iterable1", Lists.newArrayList(item1, item2));

        IterableValorisationData.IterableValorisationItemData item3 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key6}}"), new KeyValueValorisationData("field2", "value {{key7}}")));
        IterableValorisationData.IterableValorisationItemData item4 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key8}}"), new KeyValueValorisationData("field2", "value {{key9}}")));

        IterableValorisationData iv2 = new IterableValorisationData("iterable2", Lists.newArrayList(item3, item4));

        PropertiesData properties = new PropertiesData(kvp, Sets.newHashSet(iv1, iv2));

        //Slurp'em
        InstanceModel model = properties.generateInstanceModel(null);

        //Model should contain 3 keys
        assertThat(model.getKeys().size()).isEqualTo(9);
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key1", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key2", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key3", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key4", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key5", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key6", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key7", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key8", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key9", ""))).isTrue();
    }

    @Test
    public void get_instance_model_should_return_model_with_less_keys_if_one_already_belongs_to_properties(){
        //Create the properties
        Set<KeyValueValorisationData> kvp = Sets.newHashSet();

        kvp.add(new KeyValueValorisationData("name1", "value {{key1}}"));
        kvp.add(new KeyValueValorisationData("name2", "value {{key2}}"));
        kvp.add(new KeyValueValorisationData("name3", "value {{name1}}"));

        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{name1}}")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key5}}")));

        IterableValorisationData iv1 = new IterableValorisationData("iterable1", Lists.newArrayList(item1, item2));

        IterableValorisationData.IterableValorisationItemData item3 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{name1}}"), new KeyValueValorisationData("field2", "value {{key7}}")));
        IterableValorisationData.IterableValorisationItemData item4 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key8}}"), new KeyValueValorisationData("field2", "value {{name1}}")));

        IterableValorisationData iv2 = new IterableValorisationData("iterable2", Lists.newArrayList(item3, item4));

        PropertiesData properties = new PropertiesData(kvp, Sets.newHashSet(iv1, iv2));

        //Slurp'em
        InstanceModel model = properties.generateInstanceModel(null);

        //Model should contain 3 keys
        assertThat(model.getKeys().size()).isEqualTo(5);
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key1", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key2", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key5", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key7", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key8", ""))).isTrue();
    }

    @Test
    public void get_instance_model_should_ignore_whitespaces_on_properties_names(){
        // The valuations, simple
        Set<KeyValueValorisationData> kvp = Sets.newHashSet();

        kvp.add(new KeyValueValorisationData("name1", "value of name without space on instance property name {{instance}}"));
        kvp.add(new KeyValueValorisationData("name2", "value of name with a space at the start of instance property name {{ instance}}"));
        kvp.add(new KeyValueValorisationData("name3", "value of name with spaces at start and end of instance property name {{  instance   }}"));

        //The valuations, iterable
        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value with spaces {{  instance }}")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value without spaces {{instance}}")));

        IterableValorisationData iv1 = new IterableValorisationData("iterable1", Lists.newArrayList(item1, item2));

        IterableValorisationData.IterableValorisationItemData item3 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value with spaces {{ otherInstance}}"), new KeyValueValorisationData("field2", "value with spaces {{ otherInstance  }}")));
        IterableValorisationData.IterableValorisationItemData item4 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value with spaces {{  otherInstance  }}"), new KeyValueValorisationData("field2", "value without spaces {{otherInstance}}")));

        IterableValorisationData iv2 = new IterableValorisationData("iterable2", Lists.newArrayList(item3, item4));

        // The properties
        PropertiesData properties = new PropertiesData(kvp, Sets.newHashSet(iv1, iv2));

        // Instance model
        InstanceModel model = properties.generateInstanceModel(null);

        assertThat(model.getKeys().size()).isEqualTo(2);
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("instance", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("otherInstance", ""))).isTrue();
    }

    @Test
    public void get_instance_model_should_return_model_with_keys_refering_to_themselves(){
        //Create the properties
        Set<KeyValueValorisationData> kvp = Sets.newHashSet();

        kvp.add(new KeyValueValorisationData("name1", "value {{name3}}"));
        kvp.add(new KeyValueValorisationData("name2", "value {{key2}}"));
        kvp.add(new KeyValueValorisationData("name3", "value {{name3}}"));

        PropertiesData properties = new PropertiesData(kvp, Sets.newHashSet());

        //Slurp'em
        InstanceModel model = properties.generateInstanceModel(null);

        //Model should contain 3 keys
        assertThat(model.getKeys().size()).isEqualTo(2);
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("name3", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key2", ""))).isTrue();
    }

    @Test
    public void get_instance_model_should_not_produce_instance_properties_when_global_properties_already_exists() {
        //Create the module properties
        Set<KeyValueValorisationData> kvp = Sets.newHashSet();

        kvp.add(new KeyValueValorisationData("name1", "value {{key1}}"));
        kvp.add(new KeyValueValorisationData("name2", "value {{key2}}"));
        kvp.add(new KeyValueValorisationData("name3", "value {{key3}}"));

        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key4}}")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key5}}")));

        IterableValorisationData iv1 = new IterableValorisationData("iterable1", Lists.newArrayList(item1, item2));

        IterableValorisationData.IterableValorisationItemData item3 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key6}}"), new KeyValueValorisationData("field2", "value {{key7}}")));
        IterableValorisationData.IterableValorisationItemData item4 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value {{key8}}"), new KeyValueValorisationData("field2", "value {{key9}}")));

        IterableValorisationData iv2 = new IterableValorisationData("iterable2", Lists.newArrayList(item3, item4));

        PropertiesData properties = new PropertiesData(kvp, Sets.newHashSet(iv1, iv2));

        //Create some global properties
        Set<KeyValueValorisationData> globalKvp = Sets.newHashSet();

        globalKvp.add(new KeyValueValorisationData("key1", "value"));
        globalKvp.add(new KeyValueValorisationData("key2", "value"));
        globalKvp.add(new KeyValueValorisationData("key5", "value"));
        globalKvp.add(new KeyValueValorisationData("key7", "value"));
        globalKvp.add(new KeyValueValorisationData("key9", "value"));

        //Slurp'em
        InstanceModel model = properties.generateInstanceModel(globalKvp);

        //Model should contain 3 keys
        assertThat(model.getKeys().size()).isEqualTo(4);
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key3", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key4", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key6", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("key8", ""))).isTrue();
    }

    @Test
    public void get_instance_model_should_not_produce_instance_properties_when_global_properties_already_exists_ignoring_whitespaces(){
        // Valuations, simple
        Set<KeyValueValorisationData> kvp = Sets.newHashSet();

        kvp.add(new KeyValueValorisationData("name1", "value with space at start {{ global}}"));
        kvp.add(new KeyValueValorisationData("name2", "value with space at end {{global }}"));
        kvp.add(new KeyValueValorisationData("name3", "value with space everywhere {{ global }}"));
        kvp.add(new KeyValueValorisationData("name4", "value with instance {{instance}}"));
        kvp.add(new KeyValueValorisationData("name5", "value with instance with some spaces {{ instance }}")); // normally already tested

        //The valuations, iterable
        IterableValorisationData.IterableValorisationItemData item1 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value with spaces {{  otherInstance }}")));
        IterableValorisationData.IterableValorisationItemData item2 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value without spaces {{instance }}")));

        IterableValorisationData iv1 = new IterableValorisationData("iterable1", Lists.newArrayList(item1, item2));

        IterableValorisationData.IterableValorisationItemData item3 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("field1", "value with spaces {{ global}}"), new KeyValueValorisationData("field2", "value with spaces {{ global  }}")));
        IterableValorisationData.IterableValorisationItemData item4 = new IterableValorisationData.IterableValorisationItemData("blockOfProperties2", Sets.newHashSet(new KeyValueValorisationData("field1", "value with spaces {{  global  }}"), new KeyValueValorisationData("field2", "value without spaces {{global}}")));

        IterableValorisationData iv2 = new IterableValorisationData("iterable2", Lists.newArrayList(item3, item4));

        // Properties
        PropertiesData properties = new PropertiesData(kvp, Sets.newHashSet(iv1, iv2));

        // Global properties
        Set<KeyValueValorisationData> globals = Sets.newHashSet();
        globals.add(new KeyValueValorisationData("  global      ", "global value")); //a little bit exaggerated, but can't trust users ;)

        // Get instance model with globals
        InstanceModel model = properties.generateInstanceModel(globals);

        assertThat(model.getKeys().size()).isEqualTo(2);
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("instance", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("otherInstance", ""))).isTrue();
        assertThat(model.getKeys().contains(new KeyValuePropertyModel("global", ""))).isFalse();

    }
}