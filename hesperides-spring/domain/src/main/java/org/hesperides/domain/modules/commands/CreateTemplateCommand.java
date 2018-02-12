package org.hesperides.domain.modules.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.hesperides.domain.modules.Module;
import org.hesperides.domain.modules.Template;

@Value
public class CreateTemplateCommand {

    @TargetAggregateIdentifier
    Module.Key moduleKey;

    Template template;
}
