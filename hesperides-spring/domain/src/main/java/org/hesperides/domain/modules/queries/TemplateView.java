package org.hesperides.domain.modules.queries;

import lombok.Value;

@Value
public class TemplateView {
    String name;
    String namespace;
    String filename;
    String location;
}
