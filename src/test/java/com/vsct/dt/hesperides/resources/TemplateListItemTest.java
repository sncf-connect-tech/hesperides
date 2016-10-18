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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.templating.Template;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import java.io.IOException;

import static com.vsct.dt.hesperides.TestUtils.flattenJSON;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by william_montaz on 29/08/14.
 */
public class TemplateListItemTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws IOException {
        final Template hesperidesTemplate = new Template("technos.nodejs.0","instance.sh", "{{INSTANCE_NAME}}.sh", "{{HOME}}/{{INSTANCE_NAME}}", "some_content", null, 1L);

        final TemplateListItem templateListItem = new TemplateListItem(hesperidesTemplate);

        assertThat(MAPPER.writeValueAsString(templateListItem)).isEqualTo(flattenJSON("fixtures/business/template_list_item.json"));

    }

}
