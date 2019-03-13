package org.hesperides.core.presentation.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Value
@AllArgsConstructor
public final class ModuleIO {

    public final static String WORKINGCOPY = "workingcopy";
    public final static String RELEASE = "release";

    @OnlyPrintableCharacters(subject = "name")
    String name;
    @OnlyPrintableCharacters(subject = "version")
    String version;
    @SerializedName("working_copy")
    @JsonProperty("working_copy")
    Boolean isWorkingCopy;
    List<TechnoIO> technos;
    @NotNull
    @SerializedName("version_id")
    @JsonProperty("version_id")
    Long versionId;

    public ModuleIO(ModuleView moduleView) {
        this.name = moduleView.getName();
        this.version = moduleView.getVersion();
        this.isWorkingCopy = moduleView.isWorkingCopy();
        this.technos = TechnoIO.fromTechnoViews(moduleView.getTechnos());
        this.versionId = moduleView.getVersionId();
    }

    public Module toDomainInstance(List<Template> templates) {
        return new Module(new Module.Key(name, version, TemplateContainer.getVersionType(isWorkingCopy)),
                templates, TechnoIO.toDomainInstances(technos), versionId);
    }

    public Module toDomainInstance() {
        return toDomainInstance(Collections.emptyList());
    }

    public String getVersionType() {
        return getVersionType(isWorkingCopy);
    }

    static public String getVersionType(Boolean isWorkingCopy) {
        return isWorkingCopy ? WORKINGCOPY : RELEASE;
    }
}
