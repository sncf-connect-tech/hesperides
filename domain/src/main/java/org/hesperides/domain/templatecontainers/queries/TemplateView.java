package org.hesperides.domain.templatecontainers.queries;


import lombok.Value;
import org.hesperides.domain.templatecontainers.entities.Template;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

import java.util.List;
import java.util.stream.Collectors;

@Value
public class TemplateView {
    String name;
    String namespace;
    String filename;
    String location;
    String content;
    RightsView rights;
    Long versionId;

    public static List<Template> toDomainInstances(List<TemplateView> templateViews, TemplateContainer.Key key) {
        List<Template> templates = null;
        if (templateViews != null) {
            templates = templateViews.stream().map(templateView -> templateView.toDomainInstance(key)).collect(Collectors.toList());
        }
        return templates;
    }

    public Template toDomainInstance(TemplateContainer.Key key) {
        return new Template(name, filename, location, content, rights.toDomainInstance(), versionId, key);
    }

    @Value
    public static class RightsView {
        FileRightsView user;
        FileRightsView group;
        FileRightsView other;

        public Template.Rights toDomainInstance() {
            return new Template.Rights(user.toDomainInstance(), group.toDomainInstance(), other.toDomainInstance());
        }
    }

    @Value
    public static class FileRightsView {
        Boolean read;
        Boolean write;
        Boolean execute;

        public Template.FileRights toDomainInstance() {
            return new Template.FileRights(read, write, execute);
        }
    }
}
