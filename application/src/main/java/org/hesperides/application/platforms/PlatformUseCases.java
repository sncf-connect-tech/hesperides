package org.hesperides.application.platforms;

import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.platforms.commands.PlatformCommands;
import org.hesperides.domain.platforms.entities.Platform;
import org.hesperides.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.domain.platforms.queries.PlatformQueries;
import org.hesperides.domain.platforms.queries.views.*;
import org.hesperides.domain.security.User;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer.VersionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


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
        return queries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }

    public List<ApplicationSearchView> searchApplications(String input) {
        List<ApplicationSearchView> applicationSearchView = queries.searchApplications(input);

        return applicationSearchView;
    }

    public ApplicationView getApplication(String applicationName) {
        return queries.getApplication(applicationName)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationName));
    }

    public void updatePlatform(Platform.Key key, Platform newDefinition, boolean copyProps, User user) {
        if (!queries.platformExists(key)) {
            throw new PlatformNotFoundException(newDefinition.getKey());
        }

        commands.updatePlatform(key, newDefinition, copyProps, user);
    }

    public List<ModulePlatformView> getPlatformUsingModule(String moduleName, String moduleVersion, String moduleVersionType) {
        Module.Key moduleKey = new Module.Key(moduleName, moduleVersion, stringToVersionType(moduleVersionType));

        return queries.getPlatformsUsingModule(moduleKey);
    }

    /**
     * Ce use case a une spécificité au niveau de la récupération
     * des plateformes ayant un module. Le version type passé dans l'url
     * peut-être soit release, soit workingcopy.
     *
     * Toutes les autres valeurs seront considérées comme étant workingcopy
     * par défaut.
     *
     * Cette fonction est donc spécifique à ce use case et permet de gérer
     * le cas par défaut. D'autres uses cases renvoient une erreur si la
     * valeur ne correspond pas aux valeurs possible de l'énumération
     * VersionType.
     */
    private VersionType stringToVersionType(String moduleVersionType) {
        boolean versionType = !VersionType
                .release
                .toString()
                .equalsIgnoreCase(moduleVersionType);

        return TemplateContainer.getVersionType(versionType);
    }

    public List<SearchPlatformView> search(String applicationName, String platformName) {
        return queries.search(applicationName, platformName);
    }
}
