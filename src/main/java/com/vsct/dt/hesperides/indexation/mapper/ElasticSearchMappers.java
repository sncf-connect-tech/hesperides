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

package com.vsct.dt.hesperides.indexation.mapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.vsct.dt.hesperides.indexation.model.*;
import io.dropwizard.jackson.Jackson;

/**
 * Created by william_montaz on 09/12/2014.
 */
public final class ElasticSearchMappers {

    private ElasticSearchMappers(){

    }

    private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();

    private static final JavaType     ENTITY_TEMPLATE_TYPE      = OBJECT_MAPPER.getTypeFactory().constructParametricType(ElasticSearchEntity.class, TemplateIndexation.class);
    public static final  ObjectReader ES_ENTITY_TEMPLATE_READER = OBJECT_MAPPER.reader(ENTITY_TEMPLATE_TYPE);

    private static final JavaType     ENTITY_MODULE_TYPE      = OBJECT_MAPPER.getTypeFactory().constructParametricType(ElasticSearchEntity.class, ModuleIndexation.class);
    public static final  ObjectReader ES_ENTITY_MODULE_READER = OBJECT_MAPPER.reader(ENTITY_MODULE_TYPE);

    private static final JavaType     ENTITY_PLATFORM_TYPE      = OBJECT_MAPPER.getTypeFactory().constructParametricType(ElasticSearchEntity.class, PlatformIndexation.class);
    public static final  ObjectReader ES_ENTITY_PLATFORM_READER = OBJECT_MAPPER.reader(ENTITY_PLATFORM_TYPE);

    private static final JavaType     DOC_MODULE_TYPE      = OBJECT_MAPPER.getTypeFactory().constructParametricType(DocWrapper.class, ModuleIndexation.class);
    public static final  ObjectWriter ES_DOC_MODULE_WRITER = OBJECT_MAPPER.writerWithType(DOC_MODULE_TYPE);

    private static final JavaType     DOC_TEMPLATE_TYPE      = OBJECT_MAPPER.getTypeFactory().constructParametricType(DocWrapper.class, TemplateIndexation.class);
    public static final  ObjectWriter ES_DOC_TEMPLATE_WRITER = OBJECT_MAPPER.writerWithType(DOC_TEMPLATE_TYPE);

    private static final JavaType     DOC_PLATFORM_TYPE      = OBJECT_MAPPER.getTypeFactory().constructParametricType(DocWrapper.class, PlatformIndexation.class);
    public static final  ObjectWriter ES_DOC_PLATFORM_WRITER = OBJECT_MAPPER.writerWithType(DOC_PLATFORM_TYPE);

    public static final ObjectWriter MODULE_WRITER   = OBJECT_MAPPER.writerWithType(ModuleIndexation.class);
    public static final ObjectWriter TEMPLATE_WRITER = OBJECT_MAPPER.writerWithType(TemplateIndexation.class);
    public static final ObjectWriter PLATFORM_WRITER = OBJECT_MAPPER.writerWithType(PlatformIndexation.class);

}
