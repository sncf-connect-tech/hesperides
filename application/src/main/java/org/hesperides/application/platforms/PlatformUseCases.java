package org.hesperides.application.platforms;

import org.hesperides.domain.platforms.commands.PlatformCommands;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.domain.platforms.queries.PlatformQueries;
import org.hesperides.domain.platforms.queries.views.ApplicationSearchView;
import org.hesperides.domain.platforms.queries.views.ApplicationView;
import org.hesperides.domain.platforms.queries.views.PlatformView;
import org.hesperides.domain.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public void deletePlatform(Platform.Key platformKey, User user) {
        if (!queries.platformExists(platformKey)) {
            throw new PlatformNotFoundException(platformKey);
        }

        commands.deletePlatform(platformKey, user);
    }

    public PlatformView getPlatform(Platform.Key platformKey) {
        Optional<PlatformView> optionalPlatformView = queries.getOptionalPlatform(platformKey);
        if (!optionalPlatformView.isPresent()) {
            throw new PlatformNotFoundException(platformKey);
        }
        return optionalPlatformView.get();
    }


    public List<ApplicationSearchView> searchApplications(String input) {
        List<ApplicationSearchView> applicationSearchView = queries.searchApplications(input);

        return applicationSearchView;
    }

    public ApplicationView getApplication(String applicationName) {
        Optional<ApplicationView> optionalApplicationView = queries.getApplication(applicationName);

        if (!optionalApplicationView.isPresent()) {
            throw new ApplicationNotFoundException(applicationName);
        }

        return optionalApplicationView.get();
    }
}
