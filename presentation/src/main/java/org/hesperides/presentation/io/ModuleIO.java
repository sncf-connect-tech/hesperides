package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Value
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

    public Module toDomainInstance(List<Template> templates) {
        return new Module(
                new TemplateContainer.Key(name, version, workingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release),
                templates,
                technos != null ? technos.stream().map(TechnoIO::toDomainInstance).collect(Collectors.toList()) : null,
                versionId);
    }

    public Module toDomainInstance() {
        return toDomainInstance(null);
    }

    public static ModuleIO fromModuleView(ModuleView moduleView) {
        List<TechnoIO> technos = moduleView.getTechnos() != null ? moduleView.getTechnos().stream().map(TechnoIO::fromTechnoView).collect(Collectors.toList()) : null;
        return new ModuleIO(moduleView.getName(), moduleView.getVersion(), moduleView.isWorkingCopy(), technos, moduleView.getVersionId());
    }
}
