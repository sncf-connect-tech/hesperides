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

package com.vsct.dt.hesperides.storage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vsct.dt.hesperides.exception.runtime.IncoherentVersionException;
import com.vsct.dt.hesperides.exception.runtime.OutOfDateVersionException;
import io.dropwizard.jackson.JsonSnakeCase;

import javax.validation.constraints.NotNull;

/**
 * Created by william_montaz on 29/10/2014.
 * Entities are protected against concurrent writes via OptimisticLocking
 * They should extend that abstract class to be used by the aggregates
 */
@JsonSnakeCase
public abstract class DomainVersionable {

    @NotNull
    @JsonProperty("version_id")
    public long versionID; //initial default value

    protected DomainVersionable(){
        //Jackson
    }

    protected DomainVersionable(long versionID) {
        this.versionID = versionID;
    }

    public final long getVersionID() {
        return versionID;
    }

    public final long tryCompareVersionID(final DomainVersionable data) {
        return tryCompareVersionID(data.getVersionID());
    }

    public final long tryCompareVersionID(final long otherVID) {
        if (this.getVersionID() < otherVID) {
            throw new IncoherentVersionException(this.getVersionID(), otherVID);
        } else if (this.getVersionID() > otherVID) {
            throw new OutOfDateVersionException(this.getVersionID(), otherVID);
        } else return this.getVersionID();
    }
}
