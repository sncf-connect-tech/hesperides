package org.hesperides.presentation.controllers;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.stream.Collectors;

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

    Set<TechnoInput> technos;

    @SerializedName("version_id")
    Long versionId;

    Module.Key getKey() {
        return new Module.Key(name, version, isWorkingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
    }

    public Module toDomainInstance() {
        return new Module(
                new Module.Key(
                        name,
                        version,
                        isWorkingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release
                ),
                technos.stream().map(techno -> techno.toDomainInstance()).collect(Collectors.toList()),
                versionId
        );
    }

}
