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

package com.vsct.dt.hesperides.util;

import com.github.mustachejava.Mustache;
import com.google.common.collect.Maps;

import java.io.StringWriter;
import java.util.Map;

/**
 * Created by william_montaz on 09/07/14.
 */
public final class TemplateContentGenerator {

    public static TemplateMaker from(final Mustache mustacheTemplate) {
        return new TemplateMaker(mustacheTemplate);
    }

    private TemplateContentGenerator() {
    }

    private Mustache mustacheTemplate;
    private Object scope = Maps.newHashMap();

    public TemplateContentGenerator(final Mustache mustacheTemplate, final Object scope) {
        this.mustacheTemplate = mustacheTemplate;
        this.scope = scope;
    }

    public String generate() {
        StringWriter esRequest = new StringWriter();
        mustacheTemplate.execute(esRequest, scope);
        esRequest.flush();
        return esRequest.toString();
    }

    public static final class TemplateMaker {

        private final Mustache mustacheTemplate;
        private final Map<String, Object> scope = Maps.newHashMap();

        private TemplateMaker(final Mustache mustacheTemplate) {
            this.mustacheTemplate = mustacheTemplate;
        }

        public TemplateMaker put(final Map<String, ? extends Object> keyValues) {
            this.scope.putAll(keyValues);
            return this;
        }

        public TemplateMaker put(final String key, final Object value) {
            this.scope.put(key, value);
            return this;
        }

        public TemplateContentGenerator withScope(final Object objectScope) {
            return new TemplateContentGenerator(mustacheTemplate, objectScope);
        }

        public String generate() {
            TemplateContentGenerator t = new TemplateContentGenerator(mustacheTemplate, scope);
            return t.generate();
        }
    }

}

