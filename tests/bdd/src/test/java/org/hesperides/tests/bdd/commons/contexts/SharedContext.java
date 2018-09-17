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
package org.hesperides.tests.bdd.commons.contexts;

import org.hesperides.tests.bdd.commons.tools.AbstractBuilder;

import java.util.ArrayList;
import java.util.List;

public class SharedContext {

    private List<AbstractBuilder> builders = new ArrayList<>();

    public <T extends AbstractBuilder> T getBuilder(Class<T> clazz) {
        return builders.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElseGet(() -> instanciateBuilder(clazz));
    }

    private <T extends AbstractBuilder> T instanciateBuilder(Class<T> clazz) {
        T builder;
        try {
            builder = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        builders.add(builder);
        return builder;
    }
}
