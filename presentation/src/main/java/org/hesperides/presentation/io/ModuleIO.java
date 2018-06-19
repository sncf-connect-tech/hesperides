package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Value
public final class ModuleIO {
    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @NotEmpty
    String version;

    @SerializedName("working_copy")
    boolean isWorkingCopy;

    List<TechnoIO> technos;

    @SerializedName("version_id")
    Long versionId;

    public Module toDomainInstance(List<Template> templates) {
        return new Module(new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy)),
                templates, TechnoIO.toDomainInstances(technos), versionId);
    }

    public Module toDomainInstance() {
        return toDomainInstance(null);
    }

    public static ModuleIO fromModuleView(ModuleView moduleView) {
        return new ModuleIO(moduleView.getName(), moduleView.getVersion(), moduleView.isWorkingCopy(), TechnoIO.fromTechnoViews(moduleView.getTechnos()), moduleView.getVersionId());
    }
}
