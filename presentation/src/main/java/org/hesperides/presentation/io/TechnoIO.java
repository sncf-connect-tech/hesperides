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
import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
public class TechnoIO {
    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @NotEmpty
    String version;

    @SerializedName("working_copy")
    boolean workingCopy;

    public Techno toDomainInstance() {
        return new Techno(new TemplateContainer.Key(name, version, workingCopy ? TemplateContainer.VersionType.workingcopy : TemplateContainer.VersionType.release), null);
    }

    public static TechnoIO fromTechnoView(TechnoView technoView) {
        return new TechnoIO(technoView.getName(), technoView.getVersion(), technoView.isWorkingCopy());
    }
}
