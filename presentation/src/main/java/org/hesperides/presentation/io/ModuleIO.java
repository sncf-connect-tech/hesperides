package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Value
@AllArgsConstructor
public final class ModuleIO {
    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @NotEmpty
    String version;

    @SerializedName("working_copy")
    boolean workingCopy;

    List<TechnoIO> technos;

    @SerializedName("version_id")
    Long versionId;

    public ModuleIO(ModuleView moduleView) {
        this.name = moduleView.getName();
        this.version = moduleView.getVersion();
        this.workingCopy = moduleView.isWorkingCopy();
        this.technos = TechnoIO.fromTechnoViews(moduleView.getTechnos());
        this.versionId = moduleView.getVersionId();
    }

    public Module toDomainInstance(List<Template> templates) {
        return new Module(new Module.Key(name, version, TemplateContainer.getVersionType(workingCopy)),
                templates, TechnoIO.toDomainInstances(technos), versionId);
    }

    public Module toDomainInstance() {
        return toDomainInstance(null);
    }
}
