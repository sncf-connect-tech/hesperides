package org.hesperides.presentation.inputs;

import lombok.Value;
import org.hesperides.domain.templatecontainer.entities.Template;

@Value
public class RightsInput {
    FileRights user;
    FileRights group;
    FileRights other;

    public Template.Rights toDomainInstance() {
        Template.FileRights userRights = user != null ? new Template.FileRights(user.read, user.write, user.execute) : null;
        Template.FileRights groupRights = group != null ? new Template.FileRights(group.read, group.write, group.execute) : null;
        Template.FileRights otherRights = other != null ? new Template.FileRights(other.read, other.write, other.execute) : null;
        return new Template.Rights(userRights, groupRights, otherRights);
    }

    @Value
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}
