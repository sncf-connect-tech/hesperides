package org.hesperides.presentation.io;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.domain.templatecontainer.queries.TemplateView;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
public class TemplateIO {
    String namespace;
    @NotNull
    @NotEmpty
    String name;
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

    public Template toDomainInstance(TemplateContainer.Key templateContainerKey) {
        return new Template(name, filename, location, content, rights != null ? rights.toDomainInstance() : null, versionId, templateContainerKey);
    }

    public static TemplateIO fromTemplateView(TemplateView templateView) {
        return new TemplateIO(
                templateView.getNamespace(),
                templateView.getName(),
                templateView.getFilename(),
                templateView.getLocation(),
                templateView.getContent(),
                RightsIO.fromRightsView(templateView.getRights()),
                templateView.getVersionId()
        );
    }

    @Value
    public static class RightsIO {
        FileRightsIO user;
        FileRightsIO group;
        FileRightsIO other;

        public Template.Rights toDomainInstance() {
            return new Template.Rights(
                    user != null ? user.toDomainInstance() : null,
                    group != null ? group.toDomainInstance() : null,
                    other != null ? other.toDomainInstance() : null
            );
        }

        public static RightsIO fromRightsView(TemplateView.RightsView rightsView) {
            return new RightsIO(
                    FileRightsIO.fromFileRightsView(rightsView.getUser()),
                    FileRightsIO.fromFileRightsView(rightsView.getGroup()),
                    FileRightsIO.fromFileRightsView(rightsView.getOther())
            );
        }
    }

    @Value
    public static class FileRightsIO {
        Boolean read;
        Boolean write;
        Boolean execute;

        public Template.FileRights toDomainInstance() {
            return new Template.FileRights(read, write, execute);
        }

        public static FileRightsIO fromFileRightsView(TemplateView.FileRightsView fileRightsView) {
            FileRightsIO fileRightsIO = null;
            if (fileRightsView != null) {
                fileRightsIO = new FileRightsIO(fileRightsView.getRead(), fileRightsView.getWrite(), fileRightsView.getExecute());
            }
            return fileRightsIO;
        }
    }
}
