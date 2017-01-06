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

package com.vsct.dt.hesperides.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by william_montaz on 19/02/2015.
 */
public class HesperidesVersion {

    @JsonProperty(value = "name")
    private String  versionName;

    @JsonProperty(value = "working_copy")
    private boolean isWorkingCopy;

    private HesperidesVersion(){}

    public HesperidesVersion(String versionName, boolean isworkingCopy) {
        this.versionName = versionName;
        this.isWorkingCopy = isworkingCopy;
    }

    @JsonProperty(value = "name")
    public String getVersionName() {
        return versionName;
    }

    @JsonProperty(value = "working_copy")
    public boolean isWorkingCopy() {
        return isWorkingCopy;
    }

    @Override
    public String toString() {
        return "HesperidesVersion{" +
                "versionName='" + versionName + '\'' +
                ", isWorkingCopy=" + isWorkingCopy +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionName, isWorkingCopy);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HesperidesVersion other = (HesperidesVersion) obj;
        return Objects.equals(this.versionName, other.versionName)
                && Objects.equals(this.isWorkingCopy, other.isWorkingCopy);
    }

}
