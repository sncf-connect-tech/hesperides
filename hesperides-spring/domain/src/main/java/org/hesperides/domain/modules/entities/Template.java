package org.hesperides.domain.modules.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hesperides.domain.modules.queries.TemplateView;

@Value
public class Template {
    String name;
    String filename;
    String location;
    String content;
    Rights rights;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rights {
        FileRights user;
        FileRights group;
        FileRights other;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}
