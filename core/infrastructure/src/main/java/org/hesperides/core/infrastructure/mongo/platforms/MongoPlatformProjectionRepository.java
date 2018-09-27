package org.hesperides.core.infrastructure.mongo.platforms;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.platforms.documents.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoPlatformProjectionRepository implements PlatformProjectionRepository {

    private final MongoPlatformRepository platformRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoPlatformProjectionRepository(MongoPlatformRepository platformRepository, final MongoTemplate mongoTemplate) {
        this.platformRepository = platformRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onPlatformCreatedEvent(PlatformCreatedEvent event) {
        PlatformDocument platformDocument = new PlatformDocument(event.getPlatform());
        platformRepository.save(platformDocument);
    }

    @EventHandler
    @Override
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        platformRepository.deleteByKey(new PlatformKeyDocument(event.getPlatformKey()));
    }

    @EventHandler
    @Override
    public void onPlatformUpdatedEvent(PlatformUpdatedEvent event) {
        PlatformDocument platformDocument = new PlatformDocument(event.getPlatform());
        platformRepository.save(platformDocument);
    }

    @EventHandler
    @Override
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {
        final PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(event.getPlatformKey());

        // Tranformation des propriétés du domaine en documents
        final List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = new ArrayList<>();
        List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(event.getValuedProperties(), ValuedProperty.class);
        abstractValuedPropertyDocuments.addAll(ValuedPropertyDocument.fromDomainInstances(valuedProperties));
        List<IterableValuedProperty> iterableValuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(event.getValuedProperties(), IterableValuedProperty.class);
        abstractValuedPropertyDocuments.addAll(IterableValuedPropertyDocument.fromDomainInstances(iterableValuedProperties));

        // Récupération de la plateforme et contrôle de la version
        PlatformDocument platform = platformRepository.findOptionalByKey(platformKeyDocument)
                .orElseThrow(() -> new PlatformNotFoundException(event.getPlatformKey()));
        platform.setVersionId(event.getPlatformVersionId());

        // Modification des propriétés du module dans la plateforme
        platform.getDeployedModules().stream()
                .filter(moduleDocument -> moduleDocument.getPropertiesPath().equals(event.getModulePath()))
                .findAny().ifPresent(module -> {
                    module.setValuedProperties(abstractValuedPropertyDocuments);
                    platformRepository.save(platform);
                });
    }

    @EventHandler
    @Override
    public void onPlatformPropertiesUpdatedEvent(PlatformPropertiesUpdatedEvent event) {
        final PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(event.getPlatformKey());

        // Transform the properties into documents
        List<ValuedPropertyDocument> valuedProperties = event.getValuedProperties()
                .stream()
                .map(ValuedPropertyDocument::fromDomainInstance)
                .collect(Collectors.toList());

        // Retrieve platform and check its version id
        PlatformDocument platform = platformRepository.findOptionalByKey(platformKeyDocument)
                .orElseThrow(() -> new PlatformNotFoundException(event.getPlatformKey()));

        // Update platform information
        platform.setVersionId(event.getPlatformVersionId());
        platform.setValuedProperties(valuedProperties);

        platformRepository.save(platform);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Boolean onPlatformExistsByKeyQuery(PlatformExistsByKeyQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.countByKey(platformKeyDocument) > 0;
    }

    @QueryHandler
    @Override
    public Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.findOptionalByKey(platformKeyDocument)
                .map(PlatformDocument::toPlatformView);
    }

    @QueryHandler
    @Override
    public Optional<ApplicationView> onGetApplicationByNameQuery(GetApplicationByNameQuery query) {

        Optional<ApplicationView> optionalApplicationView = Optional.empty();
        List<PlatformDocument> platformDocuments = platformRepository.findAllByKeyApplicationName(query.getApplicationName());

        if (!CollectionUtils.isEmpty(platformDocuments)) {
            ApplicationView applicationView = PlatformDocument.toApplicationView(query.getApplicationName(), platformDocuments);
            optionalApplicationView = Optional.of(applicationView);
        }
        return optionalApplicationView;
    }

    @QueryHandler
    @Override
    public Optional<InstanceModelView> onGetInstanceModelQuery(GetInstanceModelQuery query) {

        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final PlatformDocument platformDocument = platformRepository.findByKeyAndFilterDeployedModulesByPropertiesPath(platformKeyDocument, query.getPath());

        // Création des instanceModelView depuis les propriétés de la première instance du module
        return Optional.ofNullable(platformDocument.getDeployedModules())
                .orElse(Collections.emptyList())
                .stream()
                .findFirst()
                .map(DeployedModuleDocument::getInstances)
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .map(InstanceDocument::toInstanceModelView);
    }

    @QueryHandler
    @Override
    public List<ModulePlatformView> onGetPlatformUsingModuleQuery(GetPlatformsUsingModuleQuery query) {

        TemplateContainer.Key moduleKey = query.getModuleKey();
        List<PlatformDocument> platformDocuments = platformRepository
                .findAllByDeployedModulesNameAndDeployedModulesVersionAndDeployedModulesIsWorkingCopy(
                        moduleKey.getName(), moduleKey.getVersion(), moduleKey.isWorkingCopy());

        return platformDocuments
                .stream()
                .map(PlatformDocument::toModulePlatformView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<SearchPlatformResultView> onSearchPlatformsQuery(SearchPlatformsQuery query) {

        String platformName = StringUtils.defaultString(query.getPlatformName(), "");

        Pageable pageable = new PageRequest(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        List<PlatformDocument> platformDocuments =
                platformRepository.findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(
                        query.getApplicationName(),
                        platformName,
                        pageable);

        return platformDocuments
                .stream()
                .map(PlatformDocument::toSearchPlatformResultView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<SearchApplicationResultView> onSearchApplicationsQuery(SearchApplicationsQuery query) {

        Pageable pageable = new PageRequest(0, 10); //TODO Sortir cette valeur dans le fichier de configuration
        List<PlatformDocument> platformDocuments = platformRepository
                .findAllByKeyApplicationNameLike(query.getApplicationName(), pageable);

        return platformDocuments
                .stream()
                .map(PlatformDocument::toSearchApplicationResultView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<AbstractValuedPropertyView> onGetDeployedModulePropertiesQuery(GetDeployedModulesPropertiesQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final PlatformDocument platformDocument = platformRepository.findByKeyAndFilterDeployedModulesByPropertiesPath(platformKeyDocument, query.getPath());
        final List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = Optional.ofNullable(platformDocument.getDeployedModules())
                .orElse(Collections.emptyList())
                .stream()
                .filter(deployedModuleDocument -> query.getPath().equals(deployedModuleDocument.getPropertiesPath()))
                .flatMap(deployedModuleDocument -> deployedModuleDocument.getValuedProperties().stream())
                .collect(Collectors.toList());
        return AbstractValuedPropertyDocument.toAbstractValuedPropertyViews(abstractValuedPropertyDocuments);
    }

    @QueryHandler
    @Override
    public List<ValuedPropertyView> onGetGlobalPropertiesQuery(final GetGlobalPropertiesQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final Optional<PlatformDocument> platformDocument = platformRepository.findOptionalByKey(platformKeyDocument);
        if (platformDocument.isPresent()) {
            return ValuedPropertyDocument.toValuedPropertyViews(platformDocument.get().getValuedProperties());
        }
        return Collections.emptyList();
    }
}
