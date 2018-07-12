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
package org.hesperides.core.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class TechnoIO {

    @OnlyPrintableCharacters(subject = "name")
    String name;
    @OnlyPrintableCharacters(subject = "version")
    String version;
    @SerializedName("working_copy")
    boolean isWorkingCopy;

    public TechnoIO(TechnoView technoView) {
        this.name = technoView.getName();
        this.version = technoView.getVersion();
        this.isWorkingCopy = technoView.isWorkingCopy();
    }

    public Techno toDomainInstance() {
        return new Techno(new Techno.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy)), Collections.emptyList());
    }

    public static List<Techno> toDomainInstances(List<TechnoIO> technoIOS) {
        return Optional.ofNullable(technoIOS)
                .orElse(Collections.emptyList())
                .stream()
                .map(TechnoIO::toDomainInstance)
                .collect(Collectors.toList());
    }

    public static List<TechnoIO> fromTechnoViews(List<TechnoView> technoViews) {
        return Optional.ofNullable(technoViews)
                .orElse(Collections.emptyList())
                .stream()
                .map(TechnoIO::new)
                .collect(Collectors.toList());
    }
}
