package org.hesperides.presentation.inputs;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.Template;

import javax.validation.constraints.NotNull;

@Value
public class TemplateInput {
    @NotNull
    String name;
    @NotNull
    String filename;
    @NotNull
    String location;
    @NotNull
    String content;
    @NotNull
    RightsInput rights;
    @NotNull
    @SerializedName("version_id")
    Long versionId;

    public Template toDomainInstance(final Module.Key moduleKey) {
        return new Template(name, filename, location, content, rights.toDomainInstance(), versionId, moduleKey);
    }
}
