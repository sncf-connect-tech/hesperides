package org.hesperides.presentation.io.templatecontainers;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.queries.TemplateView;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Value
@AllArgsConstructor
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

    public TemplateIO(TemplateView templateView) {
        this.name = templateView.getName();
        this.namespace = templateView.getNamespace();
        this.filename = templateView.getFilename();
        this.location = templateView.getLocation();
        this.content = templateView.getContent();
        this.rights = RightsIO.fromRightsView(templateView.getRights());
        this.versionId = templateView.getVersionId();
    }

    public Template toDomainInstance(TemplateContainer.Key templateContainerKey) {
        return new Template(name, filename, location, content, RightsIO.toDomainInstance(rights), versionId, templateContainerKey);
    }

    @Value
    public static class RightsIO {
        FileRightsIO user;
        FileRightsIO group;
        FileRightsIO other;

        public static Template.Rights toDomainInstance(RightsIO rightsIO) {
            //TODO Est-ce que je peux/dois utiliser un constructeur ?
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
            //TODO Est-ce que je peux/dois utiliser un constructeur ?
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
            //TODO Est-ce que je peux/dois utiliser un constructeur ?
            Template.FileRights fileRights = null;
            if (fileRightsIO != null) {
                fileRights = new Template.FileRights(fileRightsIO.getRead(), fileRightsIO.getWrite(), fileRightsIO.getExecute());
            }
            return fileRights;
        }

        public static FileRightsIO fromFileRightsView(TemplateView.FileRightsView fileRightsView) {
            //TODO Est-ce que je peux/dois utiliser un constructeur ?
            FileRightsIO fileRightsIO = null;
            if (fileRightsView != null) {
                fileRightsIO = new FileRightsIO(fileRightsView.getRead(), fileRightsView.getWrite(), fileRightsView.getExecute());
            }
            return fileRightsIO;
        }
    }
}
