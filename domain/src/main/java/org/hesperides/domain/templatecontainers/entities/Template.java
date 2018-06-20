package org.hesperides.domain.templatecontainers.entities;

import lombok.Value;

@Value
public class Template {
    String name;
    String filename;
    String location;
    String content;
    Rights rights;
    Long versionId;
    TemplateContainer.Key templateContainerKey;

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
