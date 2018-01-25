package org.hesperides.presentation.legacydtos;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
// @JsonSnakeCase //todo trouve un truc qui remplace ça.
@JsonPropertyOrder({"name", "version", "working_copy", "technos", "version_id"})
public final class Module extends DomainVersionable {

    @NotNull
    @NotEmpty
    @JsonProperty("name")
    private final String name;

    @NotNull
    @NotEmpty
    @JsonProperty("version")
    private final String version;

    @JsonProperty("working_copy")
    private final boolean workingCopy;

    @JsonProperty("technos")
    @JsonDeserialize(as = ImmutableSet.class)
    private final Set<Techno> technos;

    @JsonCreator
    public Module(@JsonProperty("name") String name,
                  @JsonProperty("version") String version,
                  @JsonProperty("working_copy") boolean isWorkingCopy,
                  @JsonProperty("technos") final Set<Techno> technos,
                  @JsonProperty("version_id") final long versionID) {
        super(versionID);
        //Maintain legacy construct
        this.name = name;
        this.version = version;
        this.workingCopy = isWorkingCopy;
        this.technos = ImmutableSet.copyOf(technos);
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
                ", workingCopy=" + workingCopy +
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
        final Module other = (Module) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.version, other.version)
                && Objects.equals(this.workingCopy, other.workingCopy)
                && Objects.equals(this.versionID, other.versionID);
    }
}
