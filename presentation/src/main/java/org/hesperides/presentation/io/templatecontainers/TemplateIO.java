package org.hesperides.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
public class TemplateIO {
    @NotNull
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

    public Template toDomainInstance(TemplateContainer.Key templateContainerKey) {
        return new Template(name, filename, location, content, RightsIO.toDomainInstance(rights), versionId, templateContainerKey);
    }

    public static TemplateIO fromTemplateView(TemplateView templateView) {
        return new TemplateIO(
                templateView.getName(),
                templateView.getNamespace(),
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

        public static Template.Rights toDomainInstance(RightsIO rightsIO) {
            Template.Rights rights = null;
            if (rightsIO != null) {
                rights = new Template.Rights(
                        FileRightsIO.toDomainInstance(rightsIO.getUser()),
                        FileRightsIO.toDomainInstance(rightsIO.getGroup()),
                        FileRightsIO.toDomainInstance(rightsIO.getOther()));
            }
            return rights;
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

        public static Template.FileRights toDomainInstance(FileRightsIO fileRightsIO) {
            Template.FileRights fileRights = null;
            if (fileRightsIO != null) {
                fileRights = new Template.FileRights(fileRightsIO.getRead(), fileRightsIO.getWrite(), fileRightsIO.getExecute());
            }
            return fileRights;
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
