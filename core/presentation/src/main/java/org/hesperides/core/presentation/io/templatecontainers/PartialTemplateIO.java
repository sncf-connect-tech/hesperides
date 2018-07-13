package org.hesperides.core.presentation.io.templatecontainers;

import lombok.Value;
import org.hesperides.core.domain.templatecontainers.queries.TemplateView;
import org.hesperides.core.presentation.io.OnlyPrintableCharacters;

import javax.validation.constraints.NotNull;

@Value
public class PartialTemplateIO implements Comparable<PartialTemplateIO> {

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

    /**
     * L'implémentation de l'interface Comparable nous permet de trier les partials templates par leur nom.
     * Ce tri est nécessaire pour les tests de non régression.
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(@NotNull PartialTemplateIO o) {
        return this.name.compareTo(o.name);
    }
}
