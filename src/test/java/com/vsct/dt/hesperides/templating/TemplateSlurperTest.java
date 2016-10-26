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

import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.vsct.dt.hesperides.templating.models.Property;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.IterablePropertyModel;
import com.vsct.dt.hesperides.templating.modules.template.TemplateSlurper;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by william_montaz on 10/07/14.
 */
public class TemplateSlurperTest {

    static String readFile(String path)
            throws IOException {
        return new String(ByteStreams.toByteArray(TemplateSlurperTest.class.getClassLoader().getResourceAsStream(path)), "UTF-8");
    }


    @Test
    public void should_produce_scope_with_key_values() throws IOException {
        //Given
        String devTemplate = readFile("template_keyvalues.txt");

        //if
        TemplateSlurper templateSlurper = new TemplateSlurper(devTemplate);
        HesperidesPropertiesModel propertiesModel = templateSlurper.generatePropertiesScope();

        //then
        assertEquals(3, propertiesModel.getKeyValueProperties().size());
        Set<String> names = propertiesModel.getKeyValueProperties().stream().map(Property::getName).collect(Collectors.toSet());
        assertThat(names).isEqualTo(Sets.newHashSet("property2", "property3", "property5"));

        Set<String> comments = propertiesModel.getKeyValueProperties().stream().map(p -> p.getComment()).collect(Collectors.toSet());
        assertThat(comments).isEqualTo(Sets.newHashSet("property2 sert a quelquechose", "property3 sert a quelquechose", "property5 sert a quelquechose"));
    }


    @Test
    public void should_produce_scope_with_maps() throws IOException {
        //Given
        String devTemplate = readFile("template_map.txt");

        //if
        TemplateSlurper templateSlurper = new TemplateSlurper(devTemplate);
        HesperidesPropertiesModel propertiesModel = templateSlurper.generatePropertiesScope();

        //then
        assertEquals(2, propertiesModel.getIterableProperties().size());
        for (IterablePropertyModel iterableProperties : propertiesModel.getIterableProperties()) {
            assertEquals(2, iterableProperties.getFields().size());
        }
    }


}
