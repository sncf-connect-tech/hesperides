/*
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
 */

package com.vsct.dt.hesperides.indexation.mapper;

/**
 * Created by jordan_kergoat on 10/10/2016.
 */
public final class SingleToBulkMapper {

    private final Integer id;
    private final String body;

    public SingleToBulkMapper(Integer id, String body) {
        this.id = id;
        this.body = body;
    }

    @Override
    public String toString() {
        String action = "{\"index\": {\"_id\": \"".concat(this.id.toString()).concat("\"}}\n");
        String body = this.body.concat("\n");
        return action.concat(body);
    }
}
