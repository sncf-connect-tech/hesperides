package org.hesperides.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.framework.DomainPrimer;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
@AllArgsConstructor
public class TemplateIO {

    @NotEmpty
    String name;
    String namespace;
    @NotNull
    String filename;
    @NotNull
    String location;
    @NotNull
    String content;
    @NotNull
    RightsIO rights;
    @NotNull
    @SerializedName("version_id")
    Long versionId;

    public TemplateIO(TemplateView templateView) {
        this.name = templateView.getName();
        this.namespace = templateView.getNamespace();
        this.filename = templateView.getFilename();
        this.location = templateView.getLocation();
        this.content = templateView.getContent();
        this.rights = new RightsIO(templateView.getRights());
        this.versionId = templateView.getVersionId();
    }

    public Template toDomainInstance(TemplateContainer.Key templateContainerKey) {
        return new Template(name, filename, location, content, DomainPrimer.toDomainInstanceOrNull(rights), versionId, templateContainerKey);
    }

    @Value
    @AllArgsConstructor
    public static class RightsIO implements DomainPrimer<Template.Rights> {

        FileRightsIO user;
        FileRightsIO group;
        FileRightsIO other;

        public RightsIO(TemplateView.RightsView rightsView) {
            this.user = new FileRightsIO(rightsView.getUser());
            this.group = new FileRightsIO(rightsView.getGroup());
            this.other = new FileRightsIO(rightsView.getOther());
        }

        public Template.Rights toDomainInstance() {
            return new Template.Rights(
                    DomainPrimer.toDomainInstanceOrNull(user),
                    DomainPrimer.toDomainInstanceOrNull(group),
                    DomainPrimer.toDomainInstanceOrNull(other)
            );
        }
    }

    @Value
    @AllArgsConstructor
    public static class FileRightsIO implements DomainPrimer<Template.FileRights> {

        Boolean read;
        Boolean write;
        Boolean execute;

        public FileRightsIO(TemplateView.FileRightsView fileRightsView) {
            this.read = fileRightsView.getRead();
            this.write = fileRightsView.getWrite();
            this.execute = fileRightsView.getExecute();
        }

        public Template.FileRights toDomainInstance() {
            return new Template.FileRights(read, write, execute);
        }
    }
}
