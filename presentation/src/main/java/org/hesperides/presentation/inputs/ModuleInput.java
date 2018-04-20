package org.hesperides.presentation.inputs;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Collections;
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

    Set<TechnoInput> technos;

    @SerializedName("version_id")
    Long versionId;

    public Module.Key getDomaineModuleKey() {
        return new Module.Key(name, version, isWorkingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
    }

    public Module toDomainInstance() {
        return new Module(
                new TemplateContainer.Key(
                        name,
                        version,
                        isWorkingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release
                ),
                /**
                 * TODO Vérifier qu'un input module ne contient jamais de technos ni de templates
                 */
                Collections.emptyList(),
                Collections.emptyList(),
                versionId
        );
    }

}
