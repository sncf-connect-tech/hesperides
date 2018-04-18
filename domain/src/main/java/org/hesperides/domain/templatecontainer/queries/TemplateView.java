package org.hesperides.domain.templatecontainer.queries;


import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;

@Value
public class TemplateView {
    String name;
    String namespace;
    String filename;
    String location;
    String content;
    Rights rights;
    @SerializedName("version_id")
    Long versionId;

    @Value
    public static class Rights {
        FileRights user;
        FileRights group;
        FileRights other;

        public static Rights fromDomain(Template.Rights rights) {
            FileRights userRights = rights.getUser() != null ? FileRights.fromDomain(rights.getUser()) : null;
            FileRights groupRights = rights.getGroup() != null ? FileRights.fromDomain(rights.getGroup()) : null;
            FileRights otherRights = rights.getOther() != null ? FileRights.fromDomain(rights.getOther()) : null;
            return new Rights(userRights, groupRights, otherRights);
        }
    }

    @Value
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;

        public static FileRights fromDomain(Template.FileRights fileRights) {
            return new FileRights(fileRights.getRead(), fileRights.getWrite(), fileRights.getExecute());
        }
    }

    public static TemplateView fromDomain(Template template) {
        return new TemplateView(
                template.getName(),
                template.getTemplateContainerKey().getNamespace(),
                template.getFilename(),
                template.getLocation(),
                template.getContent(),
                Rights.fromDomain(template.getRights()),
                template.getVersionId());
    }
}
