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
package org.hesperides.test.bdd.technos;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TechnoHistory {
    private List<TechnoBuilder> technoBuilders;

    public TechnoHistory() {
        reset();
    }

    public TechnoHistory reset() {
        technoBuilders = new ArrayList<>();
        return this;
    }

    public TechnoBuilder getFirstTechnoBuilder() {
        return technoBuilders.get(0);
    }

    public void addTechnoBuilder(TechnoBuilder technoBuilder) {
        technoBuilders.add(SerializationUtils.clone(technoBuilder));
    }

    public void removeTechnoBuilder(TechnoBuilder technoBuilderToRemove) {
        technoBuilders = technoBuilders.stream()
                .filter(existingTechnoBuilder -> !existingTechnoBuilder.equalsByKey(technoBuilderToRemove))
                .collect(Collectors.toList());
    }
}
