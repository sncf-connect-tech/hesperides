package org.hesperides.domain.modules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.net.URI;
import java.util.List;

@Value public class Module {

    @Getter
    @FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE) @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key {
        String name;
        String version;
        ModuleType versionType;

        @JsonIgnore
        public URI getURI() {
            return URI.create("/rest/modules/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }

        @Override
        public String toString() {
           return "module-" + name + "-" + version + "-" + versionType.getMinimizedForm();
        }
    }

    Key key;

    List<Techno> technos;
}
