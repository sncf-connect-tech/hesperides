package org.hesperides.application.platforms;

import org.hesperides.domain.platforms.commands.PlatformCommands;
import org.hesperides.domain.platforms.queries.PlatformQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlatformUseCases {

    private final PlatformCommands commands;
    private final PlatformQueries queries;

    @Autowired
    public PlatformUseCases(PlatformCommands commands, PlatformQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }
}
