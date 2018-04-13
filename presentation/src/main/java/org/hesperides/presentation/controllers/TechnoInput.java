package org.hesperides.presentation.controllers;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.technos.entities.Techno;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

import javax.validation.constraints.NotNull;

@Value
public class TechnoInput {
    @NotNull
    String name;
    @NotNull
    String filename;
    @NotNull
    String location;
    @NotNull
    String content;
    @NotNull
    @SerializedName("version_id")
    Long versionId;
    @NotNull
    String namespace;
    @NotNull
    RightsInput rights;

    public Techno toDomainInstance(String version, boolean isWorkingCopy) {
        return new Techno(
                new TemplateContainer.Key(
                        name,
                        version,
                        isWorkingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release
                ),
                versionId
        );
    }

    public Techno toDomainInstance() {
        if (namespace != null && !namespace.isEmpty()) {
            String[] namespaceSplit = namespace.split("#");
            return new Techno(
                    new TemplateContainer.Key(
                            name,
                            namespaceSplit[2],
                            namespaceSplit[3].equalsIgnoreCase("workingcopy") ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release
                    ),
                    versionId
            );
        } else {
            return null;
        }
    }
}
