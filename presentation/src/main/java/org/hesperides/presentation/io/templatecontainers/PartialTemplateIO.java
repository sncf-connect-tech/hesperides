package org.hesperides.presentation.io.templatecontainers;

import lombok.Value;
import org.hesperides.domain.templatecontainers.queries.TemplateView;

import javax.validation.constraints.NotNull;

@Value
public class PartialTemplateIO implements Comparable<PartialTemplateIO> {
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

    //L'implémentation de l'interface comparable nous permet de comparer (trier) les partials templates par leur nom
    //Ce triage facilite les test et permet un retour plus propre à l'utilisateur.
    @Override
    public int compareTo(@NotNull PartialTemplateIO o) {
        return this.name.compareTo(o.name);
    }
}
