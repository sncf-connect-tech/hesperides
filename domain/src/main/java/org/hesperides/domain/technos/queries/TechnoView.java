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
package org.hesperides.domain.technos.queries;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class TechnoView {
    String name;
    String version;
    @SerializedName("working_copy")
    boolean workingCopy;
    @SerializedName("version_id")
    Long versionId;
    List<TemplateView> templates;

    public Techno toDomain() {
        TemplateContainer.Key technoKey = new TemplateContainer.Key(name, version, workingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
        return new Techno(
                technoKey,
                templates != null ? templates.stream().map(templateView -> templateView.toDomain(technoKey)).collect(Collectors.toList()) : null
        );
    }
}
