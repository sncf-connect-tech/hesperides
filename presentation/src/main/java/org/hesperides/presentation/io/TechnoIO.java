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
package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class TechnoIO {
    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @NotEmpty
    String version;

    @SerializedName("working_copy")
    boolean workingCopy;

    public TechnoIO(TechnoView technoView) {
        this.name = technoView.getName();
        this.version = technoView.getVersion();
        this.workingCopy = technoView.isWorkingCopy();
    }

    public Techno toDomainInstance() {
        return new Techno(new Techno.Key(name, version, TemplateContainer.getVersionType(workingCopy)), null);
    }

    public static List<Techno> toDomainInstances(List<TechnoIO> technoIOS) {
        List<Techno> technos = null;
        if (technoIOS != null) {
            technos = technoIOS.stream().map(TechnoIO::toDomainInstance).collect(Collectors.toList());
        }
        return technos;
    }

    public static List<TechnoIO> fromTechnoViews(List<TechnoView> technoViews) {
        List<TechnoIO> technoIOS = null;
        if (technoViews != null) {
            technoIOS = technoViews.stream().map(TechnoIO::new).collect(Collectors.toList());
        }
        return technoIOS;

    }
}
