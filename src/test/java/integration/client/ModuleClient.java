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

package integration.client;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.dropwizard.jackson.JsonSnakeCase;

import com.vsct.dt.hesperides.storage.DomainVersionable;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.Techno;
import com.vsct.dt.hesperides.util.HesperidesVersion;

/**
 * Created by emeric_martineau on 15/03/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSnakeCase
@JsonPropertyOrder({"name", "version", "working_copy", "technos", "version_id"})
public final class ModuleClient extends DomainVersionable {

    @JsonProperty("name")
    private final String  name;

    @JsonProperty("version")
    private final String  version;

    @JsonProperty("working_copy")
    private final boolean workingCopy;

    @JsonProperty("technos")
    @JsonDeserialize(as = HashSet.class)
    private final Set<Techno> technos;

    @JsonCreator
    public ModuleClient(@JsonProperty("name") String name,
                  @JsonProperty("version") String version,
                  @JsonProperty("working_copy") boolean isWorkingCopy,
                  @JsonProperty("technos") final Set<Techno> technos,
                  @JsonProperty("version_id") final long versionID) {
        super(versionID);
        //Maintain legacy construct
        this.name = name;
        this.version = version;
        this.workingCopy = isWorkingCopy;
        this.technos = technos;
    }

    public ModuleClient(final ModuleKey key, final Set<Techno> technos, final long versionID) {
        super(versionID);
        //Maintain legacy construct
        this.name = key.getName();
        this.version = key.getVersion().getVersionName();
        this.workingCopy = key.getVersion().isWorkingCopy();
        this.technos = technos;
    }

    public ModuleClient(final ModuleKey key, final Set<Techno> technos) {
        this(key, technos, 1L);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isWorkingCopy() {
        return workingCopy;
    }

    public Set<Techno> getTechnos() {
        return technos;
    }

    @Override
    public String toString() {
        return "HesperidesModule{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", working_copy=" + workingCopy +
                '}';
    }

    /* Semantically equals with name, version, workingcopy and version_id */
    @Override
    public int hashCode() {
        return Objects.hash(name, version, workingCopy, versionID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ModuleClient other = (ModuleClient) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.version, other.version)
                && Objects.equals(this.workingCopy, other.workingCopy)
                && Objects.equals(this.versionID, other.versionID);
    }

    @JsonIgnore
    public ModuleKey getKey() {
        return new ModuleKey(name, new HesperidesVersion(version, workingCopy));
    }
}
