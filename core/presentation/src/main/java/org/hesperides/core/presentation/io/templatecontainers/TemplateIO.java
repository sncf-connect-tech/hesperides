package org.hesperides.core.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.commons.DomainPrimer;
import org.hesperides.core.domain.templatecontainers.entities.Template;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Optional;

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
        this.rights = Optional.ofNullable(templateView.getRights()).map(RightsIO::new).orElse(null);
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

        private RightsIO(TemplateView.RightsView rightsView) {
            this.user = Optional.ofNullable(rightsView.getUser()).map(FileRightsIO::new).orElse(null);
            this.group = Optional.ofNullable(rightsView.getGroup()).map(FileRightsIO::new).orElse(null);
            this.other = Optional.ofNullable(rightsView.getOther()).map(FileRightsIO::new).orElse(null);
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

        private FileRightsIO(TemplateView.FileRightsView fileRightsView) {
            this.read = Optional.ofNullable(fileRightsView.getRead()).orElse(null);
            this.write = Optional.ofNullable(fileRightsView.getWrite()).orElse(null);
            this.execute = Optional.ofNullable(fileRightsView.getExecute()).orElse(null);
        }

        public Template.FileRights toDomainInstance() {
            return new Template.FileRights(read, write, execute);
        }
    }
}
