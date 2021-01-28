package org.hesperides.core.infrastructure.mongo.platforms;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.AnnotationEventListenerAdapter;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventsourcing.GenericTrackedDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.MessageDecorator;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.SpringProfiles;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.commands.PlatformAggregate;
import org.hesperides.core.domain.platforms.exceptions.InexistantPlatformAtTimeException;
import org.hesperides.core.domain.platforms.exceptions.UnreplayablePlatformEventsException;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.PlatformPropertiesView;
import org.hesperides.core.domain.platforms.queries.views.properties.PropertySearchResultView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.MinimalPlatformRepository;
import org.hesperides.core.infrastructure.inmemory.platforms.InmemoryPlatformRepository;
import org.hesperides.core.infrastructure.mongo.MongoConfiguration;
import org.hesperides.core.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.core.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.core.infrastructure.mongo.platforms.documents.*;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;
import static org.hesperides.core.infrastructure.mongo.Collections.PLATFORM;

@Slf4j
@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoPlatformProjectionRepository implements PlatformProjectionRepository {

    private final MinimalPlatformRepository minimalPlatformRepository;
    private final MongoPlatformRepository platformRepository;
    private final MongoModuleRepository moduleRepository;
    private final EventStorageEngine eventStorageEngine;
    private final MongoTemplate mongoTemplate;
    private final SpringProfiles springProfiles;

    private int numberOfArchivedModuleVersions = 0;

    @Autowired
    public MongoPlatformProjectionRepository(MongoPlatformRepository platformRepository,
                                             MongoModuleRepository moduleRepository,
                                             EventStorageEngine eventStorageEngine,
                                             MongoTemplate mongoTemplate,
                                             SpringProfiles springProfiles) {
        this.minimalPlatformRepository = platformRepository;
        this.platformRepository = platformRepository;
        this.moduleRepository = moduleRepository;
        this.eventStorageEngine = eventStorageEngine;
        this.mongoTemplate = mongoTemplate;
        this.springProfiles = springProfiles;
    }

    private MongoPlatformProjectionRepository(MinimalPlatformRepository minimalPlatformRepository) {
        this.minimalPlatformRepository = minimalPlatformRepository;
        this.platformRepository = null;
        this.moduleRepository = null;
        this.eventStorageEngine = null;
        this.mongoTemplate = null;
        this.springProfiles = null;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (springProfiles != null && springProfiles.isActive(MONGO)) {
            MongoConfiguration.ensureCaseInsensitivity(mongoTemplate, PLATFORM);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    @Timed
    public void onPlatformCreatedEvent(PlatformCreatedEvent event) {
        PlatformDocument platformDocument = new PlatformDocument(event.getPlatformId(), event.getPlatform());
        // Il arrive que les propriétés d'un module déployé ne soient pas valorisées par la suite,
        // cela ne doit pas empêcher de tenir compte des valeurs par défaut:
        platformDocument.getActiveDeployedModules()
                .forEach(deployedModuleDocument ->
                        completePropertiesWithMustacheContent(deployedModuleDocument.getValuedProperties(), deployedModuleDocument)
                );
        platformDocument.buildInstancesModelAndSave(minimalPlatformRepository);
    }

    @EventHandler
    @Override
    @Timed
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        minimalPlatformRepository.deleteById(event.getPlatformId());
    }

    @EventHandler
    @Override
    @Timed
    public void onPlatformUpdatedEvent(PlatformUpdatedEvent event) {
        /*
         * Architecturalement parlant, ce code est placé ici à cause de la nécessité de rejouer
         * la logique métier des évènements issus du batch de migration.
         *
         * A noter qu'une payload vide va écraser d'éventuels modules, instances
         * et valorisations de propriétés d'instances précédement présents dans la plateforme.
         * Par contre, les valorisations de propriétés globales ou de modules sont préservées.
         */
        PlatformDocument newPlatformDocument = new PlatformDocument(event.getPlatformId(), event.getPlatform());
        minimalPlatformRepository.findById(event.getPlatformId()).ifPresent(platformDocument -> {
            platformDocument.setVersion(newPlatformDocument.getVersion());
            platformDocument.setProductionPlatform(newPlatformDocument.isProductionPlatform());
            platformDocument.updateModules(newPlatformDocument.getDeployedModules(), event.getCopyPropertiesForUpgradedModules(), numberOfArchivedModuleVersions);

            platformDocument.setVersionId(newPlatformDocument.getVersionId());

            platformDocument.getActiveDeployedModules()
                    .forEach(deployedModuleDocument ->
                            completePropertiesWithMustacheContent(deployedModuleDocument.getValuedProperties(), deployedModuleDocument)
                    );
            platformDocument.buildInstancesModelAndSave(minimalPlatformRepository);
        });
    }

    @EventHandler
    @Override
    @Timed
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {
        // Tranformation des propriétés du domaine en documents
        final List<AbstractValuedPropertyDocument> abstractValuedProperties = AbstractValuedPropertyDocument.fromAbstractDomainInstances(event.getValuedProperties());

        // Récupération de la plateforme et mise à jour de la version
        Optional<PlatformDocument> optPlatformDocument = minimalPlatformRepository.findById(event.getPlatformId());
        if (!optPlatformDocument.isPresent()) {
            throw new NotFoundException("Platform not found - module properties update impossible - platform ID: " + event.getPlatformId());
        }
        PlatformDocument platformDocument = optPlatformDocument.get();
        platformDocument.setVersionId(event.getPlatformVersionId());

        // Modification des propriétés du module dans la plateforme
        platformDocument.getActiveDeployedModules()
                .filter(currentDeployedModuleDocument -> currentDeployedModuleDocument.getPropertiesPath().equals(event.getPropertiesPath()))
                .findAny().ifPresent(deployedModuleDocument -> {
            updateDeployedModuleVersionId(event.getPropertiesVersionId(), deployedModuleDocument);
            completePropertiesWithMustacheContent(abstractValuedProperties, deployedModuleDocument);
        });

        platformDocument.buildInstancesModelAndSave(minimalPlatformRepository);
    }

    private void updateDeployedModuleVersionId(Long deployedModuleVersionId, DeployedModuleDocument deployedModuleDocument) {
        deployedModuleDocument.setPropertiesVersionId(deployedModuleVersionId);
    }

    private void completePropertiesWithMustacheContent(List<AbstractValuedPropertyDocument> abstractValuedProperties,
                                                       DeployedModuleDocument deployedModuleDocument) {
        if (moduleRepository == null) {
            // Cas du InmemoryPlatformRepository
            deployedModuleDocument.setValuedProperties(abstractValuedProperties);
        } else {
            // Récupérer le model du module afin d'attribuer à chaque
            // propriété valorisée la définition initiale de la propriété
            // (ex: {{prop | @required}} => "prop | @required")
            Module.Key moduleKey = new Module.Key(deployedModuleDocument.getName(), deployedModuleDocument.getVersion(), TemplateContainer.getVersionType(deployedModuleDocument.isWorkingCopy()));
            KeyDocument moduleKeyDocument = new KeyDocument(moduleKey);
            List<AbstractPropertyDocument> modulePropertiesModel = moduleRepository
                    .findPropertiesByModuleKey(moduleKeyDocument)
                    .map(ModuleDocument::getProperties)
                    .orElseGet(Collections::emptyList);
            deployedModuleDocument.setValuedProperties(AbstractValuedPropertyDocument.completePropertiesWithMustacheContent(abstractValuedProperties, modulePropertiesModel));
        }
    }

    @EventHandler
    @Override
    @Timed
    public void onPlatformPropertiesUpdatedEvent(PlatformPropertiesUpdatedEvent event) {

        // Transform the properties into documents
        List<ValuedPropertyDocument> valuedProperties = event.getValuedProperties()
                .stream()
                .map(ValuedPropertyDocument::new)
                .collect(Collectors.toList());

        // Retrieve platform

        Optional<PlatformDocument> optPlatformDocument = minimalPlatformRepository.findById(event.getPlatformId());
        if (!optPlatformDocument.isPresent()) {
            throw new NotFoundException("Platform not found - platform properties update impossible - platform ID: " + event.getPlatformId());
        }
        PlatformDocument platformDocument = optPlatformDocument.get();

        // Update platform information
        platformDocument.setVersionId(event.getPlatformVersionId());
        platformDocument.setGlobalProperties(valuedProperties);
        platformDocument.setGlobalPropertiesVersionId(event.getGlobalPropertiesVersionId());
        platformDocument.buildInstancesModelAndSave(minimalPlatformRepository);
    }

    @EventHandler
    @Override
    @Timed
    public PlatformView onRestoreDeletedPlatformEvent(RestoreDeletedPlatformEvent event) {
        PlatformDocument platformDocument = getPlatformAtPointInTime(event.getPlatformId(), null);
        platformDocument.getActiveDeployedModules()
                .forEach(deployedModuleDocument ->
                        completePropertiesWithMustacheContent(deployedModuleDocument.getValuedProperties(), deployedModuleDocument)
                );
        minimalPlatformRepository.save(platformDocument);
        return platformDocument.toPlatformView();
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    @Timed
    public Optional<String> onGetPlatformIdFromKeyQuery(GetPlatformIdFromKeyQuery query) {
        PlatformKeyDocument keyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository
                .findOptionalIdByKey(keyDocument)
                .map(PlatformDocument::getId);
    }

    @QueryHandler
    @Override
    @Timed
    public Optional<String> onGetPlatformIdFromEvents(GetPlatformIdFromEvents query) {
        // On recherche dans TOUS les événements un PlatformEventWithPayload ayant la bonne clef.
        // On se protège en terme de perfs en n'effectuant cette recherche que sur les 7 derniers jours.
        Instant todayLastWeek = Instant.ofEpochSecond(System.currentTimeMillis() / 1000 - 7 * 24 * 60 * 60);
        Stream<? extends TrackedEventMessage<?>> abstractEventStream = eventStorageEngine.readEvents(eventStorageEngine.createTokenAt(todayLastWeek), false);
        Optional<PlatformDeletedEvent> lastMatchingPlatformEvent = abstractEventStream
                .map(GenericTrackedDomainEventMessage.class::cast)
                .filter(msg -> PlatformAggregate.class.getSimpleName().equals(msg.getType()))
                .map(MessageDecorator::getPayload)
                // On part du principe qu'une plateforme à restaurer a forcément été supprimée,
                // on peut donc n'effectuer la recherche que sur l'évènement `PlatformDeletedEvent`
                .filter(PlatformDeletedEvent.class::isInstance)
                .map(PlatformDeletedEvent.class::cast)
                .filter(platformEvent -> platformEvent.getPlatformKey().getApplicationName().equalsIgnoreCase(query.getPlatformKey().getApplicationName()) &&
                        platformEvent.getPlatformKey().getPlatformName().equalsIgnoreCase(query.getPlatformKey().getPlatformName()))
                .reduce((first, second) -> second); // On récupère le dernier élément
        return lastMatchingPlatformEvent.map(PlatformDeletedEvent::getPlatformId);
    }

    @QueryHandler
    @Override
    @Timed
    public Optional<PlatformView> onGetPlatformByIdQuery(GetPlatformByIdQuery query) {
        return minimalPlatformRepository.findById(query.getPlatformId()).map(PlatformDocument::toPlatformView);
    }

    @QueryHandler
    @Override
    @Timed
    public Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.findOptionalByKey(platformKeyDocument)
                .map(PlatformDocument::toPlatformView);
    }

    @QueryHandler
    @Override
    @Timed
    public PlatformView onGetPlatformAtPointInTimeQuery(GetPlatformAtPointInTimeQuery query) {
        return getPlatformAtPointInTime(query.getPlatformId(), query.getTimestamp()).toPlatformView();
    }

    @QueryHandler
    @Override
    @Timed
    public Boolean onPlatformExistsByKeyQuery(PlatformExistsByKeyQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.existsByKey(platformKeyDocument);
    }

    @QueryHandler
    @Override
    @Timed
    public Optional<ApplicationView> onGetApplicationByNameQuery(GetApplicationByNameQuery query) {
        List<PlatformDocument> platformDocuments = query.getHidePlatformsModules()
                ? platformRepository.findPlatformsForApplicationAndExcludeModules(query.getApplicationName())
                : platformRepository.findAllByKeyApplicationName(query.getApplicationName());

        return Optional.ofNullable(CollectionUtils.isEmpty(platformDocuments) ? null
                : PlatformDocument.toApplicationView(query.getApplicationName(), platformDocuments));
    }

    @QueryHandler
    @Override
    @Timed
    public List<ModulePlatformView> onGetPlatformUsingModuleQuery(GetPlatformsUsingModuleQuery query) {
        TemplateContainer.Key moduleKey = query.getModuleKey();
        List<PlatformDocument> platformDocuments = platformRepository
                .findPlatformsUsingModule(
                        moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy());
        return Optional.ofNullable(platformDocuments)
                .map(List::stream)
                .orElse(Stream.empty())
                .map(PlatformDocument::toModulePlatformView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<SearchApplicationResultView> onGetApplicationNamesQuery(GetApplicationNamesQuery query) {
        List<PlatformDocument> platformDocuments = platformRepository.listApplicationNames();
        return Optional.ofNullable(platformDocuments)
                .map(List::stream)
                .orElse(Stream.empty())
                .map(PlatformDocument::toSearchApplicationResultView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<SearchApplicationResultView> onSearchApplicationsQuery(SearchApplicationsQuery query) {
        List<PlatformDocument> platformDocuments = platformRepository
                .findAllByKeyApplicationNameLike(query.getApplicationName());
        return Optional.ofNullable(platformDocuments)
                .map(List::stream)
                .orElse(Stream.empty())
                .map(PlatformDocument::toSearchApplicationResultView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<SearchPlatformResultView> onSearchPlatformsQuery(SearchPlatformsQuery query) {
        String platformName = StringUtils.defaultString(query.getPlatformName(), "");
        List<PlatformDocument> platformDocuments =
                platformRepository.findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(
                        query.getApplicationName(),
                        platformName);
        return Optional.ofNullable(platformDocuments)
                .map(List::stream)
                .orElse(Stream.empty())
                .map(PlatformDocument::toSearchPlatformResultView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<String> onGetInstancesModelQuery(GetInstancesModelQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final Optional<PlatformDocument> platformDocument = platformRepository
                .findModuleByPropertiesPath(platformKeyDocument, query.getPropertiesPath());

        return platformDocument
                .map(PlatformDocument::getActiveDeployedModules)
                .orElse(Stream.empty())
                .flatMap(deployedModuleDocument -> Optional.ofNullable(deployedModuleDocument.getInstancesModel())
                        .orElseGet(Collections::emptyList)
                        .stream())
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<ValuedPropertyView> onGetGlobalPropertiesQuery(final GetGlobalPropertiesQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.findGlobalPropertiesByPlatformKey(platformKeyDocument)
                .map(PlatformDocument::getGlobalProperties)
                .map(ValuedPropertyDocument::toValuedPropertyViews)
                .orElseGet(Collections::emptyList);
    }

    @QueryHandler
    @Override
    @Timed
    public Boolean onInstanceExistsQuery(InstanceExistsQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.findModuleByPropertiesPath(platformKeyDocument, query.getPropertiesPath())
                .map(PlatformDocument::getActiveDeployedModules)
                .orElse(Stream.empty())
                .map(DeployedModuleDocument::getInstances)
                .flatMap(List::stream)
                .anyMatch(instance -> instance.getName().equals(query.getInstanceName()));
    }

    @Override
    @Timed
    public Boolean onApplicationExistsQuery(ApplicationExistsQuery query) {
        return platformRepository.existsByKeyApplicationName(query.getApplicationName());
    }

    @QueryHandler
    @Override
    @Timed
    public List<ApplicationView> onGetAllApplicationsDetailQuery(GetAllApplicationsDetailQuery query) {

        return Optional.ofNullable(platformRepository.findAll()).stream()
                .flatMap(Collection::stream)
                .map(PlatformDocument::toPlatformView)
                .collect(groupingBy(PlatformView::getApplicationName))
                .entrySet()
                .stream()
                .map(entry -> new ApplicationView(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    @QueryHandler
    @Override
    @Timed
    public Boolean onIsProductionPlatformQuery(IsProductionPlatformQuery query) {
        return platformRepository.existsByIdAndIsProductionPlatform(query.getPlatformId(), true);
    }

    @QueryHandler
    @Override
    @Timed
    public List<PlatformPropertiesView> onFindAllApplicationsPropertiesQuery(FindAllApplicationsPropertiesQuery query) {
        return platformRepository.findAllApplicationsPropertiesQuery().stream()
                .map(PlatformDocument::toPlatformPropertiesView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    @Timed
    public List<PropertySearchResultView> onSearchPropertiesQuery(SearchPropertiesQuery query) {
        List<PlatformDocument> platformDocuments = platformRepository
                .findPlatformsByPropertiesNameAndValue(query.getPropertyName(), query.getPropertyValue());

        return platformDocuments.stream()
                .map(platformDocument -> platformDocument.filterToPropertySearchResultViews(
                        query.getPropertyName(), query.getPropertyValue()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private PlatformDocument getPlatformAtPointInTime(String platformId, Long timestamp) {
        DomainEventStream eventStream = eventStorageEngine.readEvents(platformId).filter(domainEventMessage ->
                (timestamp == null || domainEventMessage.getTimestamp().toEpochMilli() <= timestamp)
                        && !domainEventMessage.getPayloadType().equals(RestoreDeletedPlatformEvent.class)
        );
        InmemoryPlatformRepository inmemoryPlatformRepository = new InmemoryPlatformRepository();
        AnnotationEventListenerAdapter eventHandlerAdapter = new AnnotationEventListenerAdapter(new MongoPlatformProjectionRepository(inmemoryPlatformRepository));
        boolean zeroEventsBeforeTimestamp = true;
        while (eventStream.hasNext()) {
            zeroEventsBeforeTimestamp = false;
            try {
                EventMessage<?> event = eventStream.next();
                eventHandlerAdapter.handle(event);
            } catch (Exception error) {
                throw new UnreplayablePlatformEventsException(timestamp, error);
            }
        }
        if (zeroEventsBeforeTimestamp) {
            throw new InexistantPlatformAtTimeException(timestamp);
        }
        return inmemoryPlatformRepository.getCurrentPlatformDocument();
    }
}
