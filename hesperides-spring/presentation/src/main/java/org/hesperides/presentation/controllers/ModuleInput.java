package org.hesperides.presentation.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.ModuleType;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"name", "version", "working_copy", "technos"})
public final class ModuleInput {

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
    public ModuleInput(@JsonProperty("name") String name,
                       @JsonProperty("version") String version,
                       @JsonProperty("working_copy") boolean isWorkingCopy,
                       @JsonProperty("technos") final Set<Techno> technos) {
        this.name = name;
        this.version = version;
        this.workingCopy = isWorkingCopy;
        this.technos = technos != null ? ImmutableSet.copyOf(technos) : ImmutableSet.of();
    }

    Module.Key getKey() {
        return new Module.Key(name, version, workingCopy ? ModuleType.workingcopy : ModuleType.release);
    }

    /**
     * Created by william_montaz on 10/12/2014.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    //@JsonSnakeCase
    @JsonPropertyOrder({"name", "version", "working_copy"})
    public static final class Techno {

        @JsonProperty("version")
        private final String version;

        @JsonProperty("working_copy")
        private final boolean workingCopy;

        @JsonProperty("name")
        private final String name;

        @JsonCreator
        public Techno(@JsonProperty("name") final String name,
                      @JsonProperty("version") final String version,
                      @JsonProperty("working_copy") final boolean isWorkingCopy) {
            this.name = name;
            this.version = version;
            this.workingCopy = isWorkingCopy;
        }

        public String getVersion() {
            return version;
        }

        public boolean isWorkingCopy() {
            return workingCopy;
        }

        public String getName() {
            return name;
        }
    }
}
