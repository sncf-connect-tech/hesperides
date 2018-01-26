package org.hesperides.domain.modules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;

import java.net.URI;
import java.util.List;

@Value public class Module {

    @Value
    public static class Key {
        String name;
        String version;
        ModuleType versionType;

        @JsonIgnore
        public URI getURI() {
            return URI.create("/rest/modules/" + name + "/" + version + "/" + versionType.name().toLowerCase());
        }
    }

    Key key;

    List<Techno> technos;
}
