package org.hesperides.core.presentation.io.templatecontainers;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.presentation.io.OnlyPrintableCharacters;

@Value
@AllArgsConstructor
public class PartialTemplateIO {

    @OnlyPrintableCharacters(subject = "name")
    String name;
    String namespace;
    @OnlyPrintableCharacters(subject = "filename")
    String filename;
    @OnlyPrintableCharacters(subject = "location")
    String location;

    public PartialTemplateIO(TemplateView templateView) {
        this.name = templateView.getName();
        this.namespace = templateView.getNamespace();
        this.filename = templateView.getFilename();
        this.location = templateView.getLocation();
    }
}
