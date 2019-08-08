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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.hesperides.core.presentation.io.TechnoIO;
import org.hesperides.core.presentation.io.templatecontainers.ModelOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.test.bdd.templatecontainers.VersionTypes;
import org.hesperides.test.bdd.templatecontainers.builders.PropertyBuilder;
import org.hesperides.test.bdd.templatecontainers.builders.TemplateBuilder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TechnoBuilder implements Serializable {

    @Getter
    private String name;
    private String version;
    private Boolean isWorkingCopy;
    @Getter
    private List<TemplateBuilder> templateBuilders;
    private List<PropertyBuilder> propertyBuilders;

    public TechnoBuilder() {
        reset();
    }

    public void reset() {
        name = "test-techno";
        version = "1.0.0";
        isWorkingCopy = true;
        templateBuilders = new ArrayList<>();
        propertyBuilders = new ArrayList<>();
    }

    public TechnoBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TechnoBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public TechnoBuilder withIsWorkingCopy(boolean isWorkingCopy) {
        this.isWorkingCopy = isWorkingCopy;
        return this;
    }

    public TechnoIO build() {
        return new TechnoIO(name, version, isWorkingCopy);
    }

    public String buildNamespace() {
        return "packages#" + name + "#" + version + "#" + VersionTypes.fromIsWorkingCopy(isWorkingCopy).toUpperCase();
    }

    public void withTemplateBuilder(TemplateBuilder templateBuilder) {
        templateBuilders.add(SerializationUtils.clone(templateBuilder));
    }

    public void withPropertyBuilder(PropertyBuilder propertyBuilder) {
        propertyBuilders.add(SerializationUtils.clone(propertyBuilder));
    }

    public void withVersionType(String versionType) {
        isWorkingCopy = VersionTypes.toIsWorkingCopy(versionType);
    }

    public String getVersionType() {
        return VersionTypes.fromIsWorkingCopy(isWorkingCopy);
    }

    public ModelOutput getPropertiesModel() {
        Set<PropertyOutput> simpleProperties = propertyBuilders.stream().map(PropertyBuilder::build).collect(Collectors.toSet());
        Set<PropertyOutput> iterableProperties = Collections.emptySet();
        return new ModelOutput(simpleProperties, iterableProperties);
    }
}
