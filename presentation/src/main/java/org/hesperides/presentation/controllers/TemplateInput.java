package org.hesperides.presentation.controllers;

import lombok.Value;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.entities.Template;

import javax.validation.constraints.NotNull;

@Value
public class TemplateInput {
    @NotNull
    String name;
    @NotNull
    String filename;
    @NotNull
    String location;
    @NotNull
    String content;
    @NotNull
    Template.Rights rights;

    public Template toDomainInstance(final Module.Key moduleKey) {
        return new Template(name, filename, location, content, rights, moduleKey);
    }
}
