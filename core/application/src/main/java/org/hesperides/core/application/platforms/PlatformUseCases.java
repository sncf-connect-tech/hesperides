package org.hesperides.core.application.platforms;

import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.domain.events.commands.EventCommands;
import org.hesperides.core.domain.events.queries.EventQueries;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.exceptions.ForbiddenOperationException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.modules.queries.ModuleQueries;
import org.hesperides.core.domain.modules.queries.ModuleView;
import org.hesperides.core.domain.platforms.PlatformCreatedEvent;
import org.hesperides.core.domain.platforms.PlatformUpdatedEvent;
import org.hesperides.core.domain.platforms.commands.PlatformCommands;
import org.hesperides.core.domain.platforms.entities.DeployedModule;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.exceptions.ApplicationNotFoundException;
import org.hesperides.core.domain.platforms.exceptions.DuplicatePlatformException;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.PlatformQueries;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.queries.ApplicationDirectoryGroupsQueries;
import org.hesperides.core.domain.security.queries.views.ApplicationDirectoryGroupsView;
import org.hesperides.core.domain.technos.entities.Techno;
import org.hesperides.core.domain.technos.queries.TechnoQueries;
import org.hesperides.core.domain.technos.queries.TechnoView;
import org.hesperides.core.domain.templatecontainers.queries.TemplateContainerKeyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class PlatformUseCases {

    private final PlatformCommands platformCommands;
    private final PlatformQueries platformQueries;
    private final ModuleQueries moduleQueries;
    private final TechnoQueries technoQueries;
    private final ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries;
    private final EventCommands eventCommands;
    private final EventQueries eventQueries;
    @Value("${hesperides.events-query-size-factor}")
    private Integer eventsQuerySizeFactor;

    @Autowired
    public PlatformUseCases(PlatformCommands platformCommands,
                            PlatformQueries platformQueries,
                            ModuleQueries moduleQueries,
                            TechnoQueries technoQueries,
                            ApplicationDirectoryGroupsQueries applicationDirectoryGroupsQueries,
                            EventCommands eventCommands,
                            EventQueries eventQueries) {
        this.platformCommands = platformCommands;
        this.platformQueries = platformQueries;
        this.moduleQueries = moduleQueries;
        this.technoQueries = technoQueries;
        this.applicationDirectoryGroupsQueries = applicationDirectoryGroupsQueries;
        this.eventCommands = eventCommands;
        this.eventQueries = eventQueries;
    }

    public static boolean isRestrictedPlatform(User user, PlatformView platform) {
        return isRestrictedPlatform(platform.isProductionPlatform(), user, platform.getApplicationName());
    }

    private static boolean isRestrictedPlatform(User user, Platform platform) {
        return isRestrictedPlatform(platform.isProductionPlatform(), user, platform.getKey().getApplicationName());
    }

    public static boolean isRestrictedPlatform(boolean isProductionPlatform, User user, String applicationName) {
        return isProductionPlatform && !user.hasProductionRoleForApplication(applicationName);
    }

    public String createPlatform(Platform platform, User user) {
        if (isRestrictedPlatform(user, platform)) {
            throw new ForbiddenOperationException("Creating a production platform is reserved to production role");
        }
        if (platformQueries.platformExists(platform.getKey())) {
            throw new DuplicatePlatformException(platform.getKey());
        }
        return cleanCreatePlatform(platform, user);
    }

    public String copyPlatform(Platform newPlatform, Platform.Key existingPlatformKey, boolean copyInstancesAndProperties, User user) {
        if (platformQueries.platformExists(newPlatform.getKey())) {
            throw new DuplicatePlatformException(newPlatform.getKey());
        }
        PlatformView existingPlatform = platformQueries.getOptionalPlatform(existingPlatformKey)
                .orElseThrow(() -> new PlatformNotFoundException(existingPlatformKey));
        if ((newPlatform.isProductionPlatform() || existingPlatform.isProductionPlatform()) && !user.hasProductionRoleForApplication(newPlatform.getKey().getApplicationName())) {
            throw new ForbiddenOperationException("Creating a platform from a production platform is reserved to production role");
        }
        List<DeployedModule> deployedModules = DeployedModuleView.toDomainDeployedModules(existingPlatform.findActiveDeployedModules());
        if (!copyInstancesAndProperties) {
            deployedModules = deployedModules.stream()
                    .map(DeployedModule::copyWithoutInstancesNorProperties)
                    .collect(toList());
        }
        List<ValuedProperty> globalProperties = copyInstancesAndProperties ? ValuedPropertyView.toDomainValuedProperties(existingPlatform.getGlobalProperties()) : emptyList();
        // cf. createPlatformFromExistingPlatform in https://github.com/voyages-sncf-technologies/hesperides/blob/fix/3.0.3/src/main/java/com/vsct/dt/hesperides/applications/AbstractApplicationsAggregate.java#L156
        Platform newFullPlatform = new Platform(
                newPlatform.getKey(),
                newPlatform.getVersion(),
                newPlatform.isProductionPlatform(),
                1L,
                deployedModules,
                DeployedModule.INIT_PROPERTIES_VERSION_ID,
                globalProperties
        );
        return cleanCreatePlatform(newFullPlatform, user);
    }

    public PlatformView getPlatform(String platformId) {
        return platformQueries.getOptionalPlatform(platformId)
                .orElseThrow(() -> new PlatformNotFoundException(platformId));
    }

    public PlatformView getPlatform(Platform.Key platformKey) {
        return getPlatform(platformKey, false);
    }

    public PlatformView getPlatform(Platform.Key platformKey, boolean withPasswordFlag) {
        PlatformView platform = platformQueries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        if (withPasswordFlag) {
            boolean hasPasswords = !isEmpty(getPlatformsWithPassword(Collections.singletonList(platform)));
            platform = platform.withPasswordIndicator(hasPasswords);
        }
        return platform;
    }

    public PlatformView getPlatformAtPointInTime(Platform.Key platformKey, long timestamp) {
        String platformId = findPlatformId(platformKey);
        return platformQueries.getPlatformAtPointInTime(platformId, timestamp);
    }

    public void updatePlatform(Platform.Key platformKey, Platform newPlatform, boolean copyPropertiesForUpgradedModules, User user) {
        PlatformView existingPlatform = platformQueries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        List<DeployedModule> existingDeployedModule = DeployedModuleView.toDomainDeployedModules(existingPlatform.getDeployedModules().stream());
        newPlatform = newPlatform.retrieveExistingOrInitializePropertiesVersionIds(existingDeployedModule);

        if (!user.hasProductionRoleForApplication(platformKey.getApplicationName())) {
            if (existingPlatform.isProductionPlatform() && newPlatform.isProductionPlatform()) {
                throw new ForbiddenOperationException("Updating a production platform is reserved to production role");
            }
            if (existingPlatform.isProductionPlatform() || newPlatform.isProductionPlatform()) {
                throw new ForbiddenOperationException("Upgrading a platform to production is reserved to production role");
            }
        }
        platformCommands.updatePlatform(existingPlatform.getId(), newPlatform, copyPropertiesForUpgradedModules, user);
    }

    public void deletePlatform(Platform.Key platformKey, User user) {
        PlatformView platform = platformQueries.getOptionalPlatform(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        if (isRestrictedPlatform(user, platform)) {
            throw new ForbiddenOperationException("Deleting a production platform is reserved to production role");
        }
        platformCommands.deletePlatform(platform.getId(), platformKey, user);
    }

    public ApplicationView getApplication(String applicationName, boolean hidePlatformsModules, boolean withPasswordFlag) {
        ApplicationView applicationView = platformQueries.getApplication(applicationName, hidePlatformsModules)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationName));

        Optional<ApplicationDirectoryGroupsView> applicationDirectoryGroups = applicationDirectoryGroupsQueries.getApplicationDirectoryGroups(applicationName);
        if (applicationDirectoryGroups.isPresent()) {
            applicationView = applicationView.withDirectoryGoups(applicationDirectoryGroups.get());
        }

        if (withPasswordFlag) {
            Set<Platform.Key> platformsWithPassword = getPlatformsWithPassword(applicationView.getPlatforms());
            applicationView = applicationView.withPasswordIndicator(platformsWithPassword);
        }

        return applicationView;
    }

    private Set<Platform.Key> getPlatformsWithPassword(List<PlatformView> platforms) {

        List<Module.Key> allPlatformsModuleKeys = platforms.stream()
                .map(PlatformView::getDeployedModules)
                .flatMap(List::stream)
                .map(DeployedModuleView::getModuleKey)
                .distinct()
                .collect(toList());

        List<ModuleView> allPlatformsModules = moduleQueries.getModulesWithin(allPlatformsModuleKeys);
        List<Module.Key> modulesWithPassword = moduleQueries.getModulesWithPasswordWithin(allPlatformsModuleKeys);

        List<Techno.Key> allModulesTechnoKeys = allPlatformsModules.stream()
                // On exclut les modules dont on sait déjà qu'ils contiennent au moins un mot de passe
                .filter(module -> !modulesWithPassword.contains(module.getKey()))
                .map(ModuleView::getTechnos)
                .flatMap(List::stream)
                .map(TechnoView::getKey)
                .map(TemplateContainerKeyView::toTechnoKey)
                .distinct()
                .collect(toList());

        Map<Module.Key, List<Techno.Key>> technoKeysByModuleMap = allPlatformsModules.stream().collect(toMap(
                ModuleView::getKey,
                module -> module.getTechnos().stream()
                        .map(TechnoView::getKey)
                        .map(TemplateContainerKeyView::toTechnoKey)
                        .distinct()
                        .collect(toList())
        ));

        List<Techno.Key> technosWithPassword = technoQueries.getTechnosWithPasswordWithin(allModulesTechnoKeys);

        // Récupère la liste des modules ayant au moins une techno contenant un mot de passe
        // et la concatène avec la liste de modules contenant au moins un mot de passe
        Set<Module.Key> allModulesWithPassword = Stream.concat(modulesWithPassword.stream(),
                technoKeysByModuleMap.entrySet().stream()
                        .filter(entry -> entry.getValue().stream().anyMatch(technosWithPassword::contains))
                        .map(Map.Entry::getKey))
                .collect(Collectors.toSet());

        Map<Platform.Key, List<Module.Key>> moduleKeysByPlatformMap = platforms.stream().collect(toMap(
                PlatformView::getPlatformKey,
                platform -> platform.getDeployedModules().stream()
                        .map(DeployedModuleView::getModuleKey)
                        .distinct()
                        .collect(toList())
        ));

        return moduleKeysByPlatformMap.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(allModulesWithPassword::contains))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public List<ModulePlatformView> getPlatformsUsingModule(Module.Key moduleKey) {
        return platformQueries.getPlatformsUsingModule(moduleKey);
    }

    public List<SearchApplicationResultView> getApplicationNames() {
        return platformQueries.getApplicationNames();
    }

    public List<SearchApplicationResultView> searchApplications(String applicationName) {
        List<SearchApplicationResultView> applications;
        if (isBlank(applicationName)) {
            applications = platformQueries.getApplicationNames();
        } else {
            applications = platformQueries.searchApplications(applicationName);
        }
        return applications;
    }

    public List<SearchPlatformResultView> searchPlatforms(String applicationName, String platformName) {
        return platformQueries.searchPlatforms(applicationName, platformName);
    }

    public PlatformView restoreDeletedPlatform(Platform.Key platformKey, User user) {
        if (platformQueries.platformExists(platformKey)) {
            throw new IllegalArgumentException("Cannot restore an existing platform");
        }
        String platformId = platformQueries.getOptionalPlatformIdFromEvents(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
        platformCommands.restoreDeletedPlatform(platformId, user);
        return getPlatform(platformId);
    }

    public List<ApplicationView> getAllApplicationsDetail(boolean withPasswordFlag) {
        List<ApplicationView> applications = platformQueries.getAllApplicationsDetail();

        if (withPasswordFlag) {
            List<PlatformView> allApplicationsPlatforms = applications.stream()
                    .map(ApplicationView::getPlatforms)
                    .flatMap(List::stream)
                    .distinct()
                    .collect(toList());

            Set<Platform.Key> platformsWithPassword = getPlatformsWithPassword(allApplicationsPlatforms);
            applications = applications.stream()
                    .map(application -> application.withPasswordIndicator(platformsWithPassword))
                    .collect(toList());
        }

        return applications;
    }

    private String cleanCreatePlatform(Platform platform, User user) {
        eventCommands.cleanAggregateEvents(platform.getKey().generateHash());
        return platformCommands.createPlatform(platform, user);
    }

    public List<PlatformEventView> getPlatformEvents(Platform.Key platformKey, Integer clientPage, Integer size) {

        // Certains évènements de mise à jour de plateforme ne contiennent
        // que l'incrémentation du version_id. On ne peut donc pas appliquer
        // la pagination sur la requête à l'EventStore mais une fois qu'on a
        // la liste des vraies modifications de la plateforme.

        // Mais comme dans certains cas le nombre d'évènements est si élevé
        // que les performances se dégradent significativement.

        // Pour palier à ça, on récupère un nombre d'évènements supérieur à
        // ce qui est demandé en retour (eventsQuerySizeFactor) et si ce
        // n'est pas suffisant, on récupère les évènements suivants.

        // La pagination finale est appliquée à la toute fin (une fois qu'on
        // a suffisamment d'évènements pour renvoyer le résultat attendu).

        // Cela nous permet de gagner en performances tout en restant pertinent
        // vis à vis des données attendues. L'inconvénient est que le plus on
        // remonte dans le passé, plus le temps de traitement augmente.

        // Mais on part du principe que les évènements les plus consultés sont
        // les derniers évènements en date et cette logique ne s'applique a
        // priori que dans le cas des mises à jour d'une plateforme.

        String platformId = findPlatformId(platformKey);

        List<PlatformEventView> platformEvents = new ArrayList<>();
        List<EventView> rawEvents = new ArrayList<>();
        // On s'arrête lorsqu'on a assez d'évènements de mises à jour de la
        // plateforme à retourner, tenant compte de la pagination
        for (int queryPage = 1; platformEvents.size() < (clientPage * size); queryPage++) {

            List<EventView> newRawEvents = eventQueries.getLastToFirstEventsByTypes(
                    platformId, new Class[]{PlatformCreatedEvent.class, PlatformUpdatedEvent.class},
                    queryPage,
                    eventsQuerySizeFactor * size);

            if (isEmpty(newRawEvents)) {
                break;
            }
            rawEvents.addAll(newRawEvents);
            platformEvents = PlatformEventView.buildPlatformEvents(rawEvents);
        }
        log.debug("${queryPage} querie(s) of ${eventsQuerySizeFactor * size} platform events " +
                "to get ${size} elements for page ${clientPage} of platform update events");
        // Tri et pagination appliqués à la toute fin
        return platformEvents
                .stream()
                .sorted(comparing(PlatformEventView::getTimestamp).reversed())
                .skip((clientPage - 1) * size)
                .limit(size)
                .collect(toList());
    }

    public String findPlatformId(Platform.Key platformKey) {
        return platformQueries.getOptionalPlatformId(platformKey)
                .orElseThrow(() -> new PlatformNotFoundException(platformKey));
    }
}
