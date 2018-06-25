package org.hesperides.application.platforms;

import org.hesperides.domain.platforms.commands.PlatformCommands;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.domain.platforms.queries.PlatformQueries;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.domain.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlatformUseCases {

    private final PlatformCommands commands;
    private final PlatformQueries queries;

    @Autowired
    public PlatformUseCases(PlatformCommands commands, PlatformQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    public Platform.Key createPlatform(Platform platform, User user) {
        if (queries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return commands.createPlatform(platform, user);
    }

    public PlatformView getPlateform(Platform.Key platformKey) {
        Optional<PlatformView> optionalPlatformView = queries.getOptionalPlatform(platformKey);
        if (!optionalPlatformView.isPresent()) {
            throw new PlatformNotFoundException(platformKey);
        }
        return optionalPlatformView.get();
    }
}
