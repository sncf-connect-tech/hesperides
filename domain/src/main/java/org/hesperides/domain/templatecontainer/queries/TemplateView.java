package org.hesperides.domain.templatecontainer.queries;


import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

@Value
public class TemplateView {
    String name;
    String namespace;
    String filename;
    String location;
    String content;
    RightsView rights;
    @SerializedName("version_id")
    Long versionId;

    public Template toDomain(TemplateContainer.Key templateContainerKey) {
        return new Template(name, filename, location, content, rights.toDomain(), versionId, templateContainerKey);
    }

    @Value
    public static class RightsView {
        FileRightsView user;
        FileRightsView group;
        FileRightsView other;

        public Template.Rights toDomain() {
            return new Template.Rights(user.toDomain(), group.toDomain(), other.toDomain());
        }
    }

    @Value
    public static class FileRightsView {
        Boolean read;
        Boolean write;
        Boolean execute;

        public Template.FileRights toDomain() {
            return new Template.FileRights(read, write, execute);
        }
    }
}
