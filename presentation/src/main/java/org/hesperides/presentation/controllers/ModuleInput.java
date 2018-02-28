package org.hesperides.presentation.controllers;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Value
public final class ModuleInput {
    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @NotEmpty
    String version;

    @SerializedName("working_copy")
    boolean isWorkingCopy;

    Set<Techno> technos;

    Module.Key getKey() {
        return new Module.Key(name, version, isWorkingCopy ? Module.Type.workingcopy : Module.Type.release);
    }

    @Value
    public static final class Techno {
        String version;
        @SerializedName("working_copy")
        boolean isWorkingCopy;
        String name;
    }
}
