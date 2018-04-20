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
    RightsView rightsView;
    @SerializedName("version_id")
    Long versionId;

    public static TemplateView fromDomain(Template template, String namespacePrefix) {
        return new TemplateView(
                template.getName(),
                template.getTemplateContainerKey().getNamespace(namespacePrefix),
                template.getFilename(),
                template.getLocation(),
                template.getContent(),
                RightsView.fromDomain(template.getRights()),
                template.getVersionId());
    }

    @Value
    public static class RightsView {
        FileRightsView user;
        FileRightsView group;
        FileRightsView other;

        public static RightsView fromDomain(Template.Rights rights) {
            FileRightsView userRights = rights.getUser() != null ? FileRightsView.fromDomain(rights.getUser()) : null;
            FileRightsView groupRights = rights.getGroup() != null ? FileRightsView.fromDomain(rights.getGroup()) : null;
            FileRightsView otherRights = rights.getOther() != null ? FileRightsView.fromDomain(rights.getOther()) : null;
            return new RightsView(userRights, groupRights, otherRights);
        }
    }

    @Value
    public static class FileRightsView {
        Boolean read;
        Boolean write;
        Boolean execute;

        public static FileRightsView fromDomain(Template.FileRights fileRights) {
            return new FileRightsView(fileRights.getRead(), fileRights.getWrite(), fileRights.getExecute());
        }
    }
}
