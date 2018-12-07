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
package org.hesperides.core.application.modules;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;

public class Test {


    @org.junit.Test
    public void test() throws IOException {
        HashMap<String, Object> scopes = new HashMap();
        HashMap<String, Object> b1 = new HashMap();
        b1.put("b", "b1");
        b1.put("c", "c1");
        HashMap<String, Object> b2 = new HashMap();
        b2.put("b", "b2");
        b2.put("c", "c2");
        scopes.put("a", Arrays.asList(b1, b2));

        Writer writer = new OutputStreamWriter(System.out);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader("{{#a}}\n{{b}},{{c}}\n{{/a}}"), "example");
        mustache.execute(writer, scopes);
        writer.flush();
    }

    static class Feature {
        Feature(String description) {
            this.description = description;
        }

        String description;
    }
}
