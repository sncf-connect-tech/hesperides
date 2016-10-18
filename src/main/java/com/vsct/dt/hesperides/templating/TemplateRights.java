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

package com.vsct.dt.hesperides.templating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;

/**
 * Created by emeric_martineau on 20/10/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
public class TemplateRights {
    private final TemplateFileRights user;
    private final TemplateFileRights group;
    private final TemplateFileRights other;

    public TemplateRights(
            @JsonProperty("user") final TemplateFileRights user,
            @JsonProperty("group") final TemplateFileRights group,
            @JsonProperty("other") final TemplateFileRights other) {
        this.user = user;
        this.group = group;
        this.other = other;
    }

    public TemplateFileRights getUser() {
        return user;
    }

    public TemplateFileRights getGroup() {
        return group;
    }

    public TemplateFileRights getOther() {
        return other;
    }
}
