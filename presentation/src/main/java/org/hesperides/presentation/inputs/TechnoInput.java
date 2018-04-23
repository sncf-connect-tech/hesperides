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
package org.hesperides.presentation.inputs;

import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import java.util.Collections;

@Value
public class TechnoInput {
    String name;
    String version;
    boolean isWorkingCopy;
    TemplateInput template;

    public Techno toDomainInstance() {
        TemplateContainer.Key technoKey = new TemplateContainer.Key(name, version, isWorkingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
        return new Techno(technoKey, Collections.singletonList(template.toDomainInstance(technoKey)));
    }
}
