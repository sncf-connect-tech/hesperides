package org.hesperides.domain.templatecontainers.entities;

import lombok.Value;
import org.hesperides.domain.exceptions.OutOfDateVersionException;

@Value
public class Template {
    String name;
    String filename;
    String location;
    String content;
    Rights rights;
    Long versionId;
    TemplateContainer.Key templateContainerKey;

    public Template initializeVersionId() {
        return new Template(
                name,
                filename,
                location,
                content,
                rights,
                1L,
                templateContainerKey
        );
    }

    public void validateVersionId(Long expectedVersionId) {
        if (!versionId.equals(expectedVersionId)) {
            throw new OutOfDateVersionException(expectedVersionId, versionId);
        }
    }

    public Template incrementVersionId() {
        return new Template(
                name,
                filename,
                location,
                content,
                rights,
                versionId + 1,
                templateContainerKey
        );
    }

    @Value
    public static class Rights {
        FileRights user;
        FileRights group;
        FileRights other;
    }

    @Value
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}
