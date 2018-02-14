package org.hesperides.domain.modules.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
public class Template {

    String name;

    String filename;

    String location;

    String content;

    Template.TemplateRights rights;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateRights {
        TemplateFileRights user;
        TemplateFileRights group;
        TemplateFileRights other;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateFileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}
