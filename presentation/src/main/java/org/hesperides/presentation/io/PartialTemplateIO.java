package org.hesperides.presentation.io;

import lombok.Value;
import org.hesperides.domain.templatecontainer.queries.TemplateView;

import javax.validation.constraints.NotNull;

@Value
public class PartialTemplateIO {
    @NotNull
    String name;
    String namespace;
    @NotNull
    String filename;
    @NotNull
    String location;

    public static PartialTemplateIO fromTemplateView(TemplateView templateView) {
        return new PartialTemplateIO(
                templateView.getName(),
                templateView.getNamespace(),
                templateView.getFilename(),
                templateView.getLocation()
        );
    }
}
