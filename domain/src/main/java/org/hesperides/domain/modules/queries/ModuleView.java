package org.hesperides.domain.modules.queries;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.technos.queries.TechnoView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class ModuleView {
    String name;
    String version;
    @SerializedName("working_copy")
    boolean workingCopy;
    @SerializedName("version_id")
    Long versionId;
    List<TemplateView> templates;
    List<TechnoView> technos;

    public Module toDomain() {
        TemplateContainer.Key moduleKey = new TemplateContainer.Key(name, version, workingCopy ? TemplateContainer.Type.workingcopy : TemplateContainer.Type.release);
        return new Module(
                moduleKey,
                templates != null ? templates.stream().map(templateView -> templateView.toDomain(moduleKey)).collect(Collectors.toList()) : null,
                technos != null ? technos.stream().map(technoView -> technoView.toDomain()).collect(Collectors.toList()) : null,
                versionId
        );
    }
}
