package org.hesperides.core.infrastructure.mongo.platforms;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.entities.properties.AbstractValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.IterableValuedProperty;
import org.hesperides.core.domain.platforms.entities.properties.ValuedProperty;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.platforms.documents.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoPlatformProjectionRepository implements PlatformProjectionRepository {

    private final MongoPlatformRepository platformRepository;

    @Autowired
    public MongoPlatformProjectionRepository(MongoPlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onPlatformCreatedEvent(PlatformCreatedEvent event) {
        PlatformDocument platformDocument = new PlatformDocument(event.getId(), event.getPlatform());
        platformDocument.extractInstancePropertiesAndSave(platformRepository);
    }

    @EventHandler
    @Override
    public void onPlatformCopiedEvent(PlatformCopiedEvent event) {
        PlatformDocument existingPlatform = platformRepository.findOne(event.getExistingPlatformId());
        PlatformDocument newPlatform = new PlatformDocument(event.getNewPlatformId(), event.getNewPlatform());
        newPlatform.setDeployedModules(existingPlatform.getDeployedModules());
        newPlatform.setValuedProperties(existingPlatform.getValuedProperties());
        platformRepository.save(newPlatform);
    }

    @EventHandler
    @Override
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        platformRepository.delete(event.getId());
    }

    @EventHandler
    @Override
    public void onPlatformUpdatedEvent(PlatformUpdatedEvent event) {
        PlatformDocument platformDocument = new PlatformDocument(event.getId(), event.getPlatform());
        platformDocument.extractInstancePropertiesAndSave(platformRepository);
    }

    @EventHandler
    @Override
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {

        //TODO Peut-on faire ces traitements dans le domaine ?

        // Tranformation des propriétés du domaine en documents
        final List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = new ArrayList<>();
        List<ValuedProperty> valuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(event.getValuedProperties(), ValuedProperty.class);
        abstractValuedPropertyDocuments.addAll(ValuedPropertyDocument.fromDomainInstances(valuedProperties));
        List<IterableValuedProperty> iterableValuedProperties = AbstractValuedProperty.filterAbstractValuedPropertyWithType(event.getValuedProperties(), IterableValuedProperty.class);
        abstractValuedPropertyDocuments.addAll(IterableValuedPropertyDocument.fromDomainInstances(iterableValuedProperties));

        // Récupération de la plateforme et mise à jour de la version
        PlatformDocument platformDocument = platformRepository.findOne(event.getId());
        platformDocument.setVersionId(event.getPlatformVersionId());

        // Modification des propriétés du module dans la plateforme
        platformDocument.getDeployedModules().stream()
                .filter(moduleDocument -> moduleDocument.getPropertiesPath().equals(event.getModulePath()))
                .findAny().ifPresent(module -> {
            module.setValuedProperties(abstractValuedPropertyDocuments);
            platformDocument.extractInstancePropertiesAndSave(platformRepository);
        });
    }

    @EventHandler
    @Override
    public void onPlatformPropertiesUpdatedEvent(PlatformPropertiesUpdatedEvent event) {

        // Transform the properties into documents
        List<ValuedPropertyDocument> valuedProperties = event.getValuedProperties()
                .stream()
                .map(ValuedPropertyDocument::new)
                .collect(Collectors.toList());

        // Retrieve platform
        PlatformDocument platformDocument = platformRepository.findOne(event.getId());

        // Update platform information
        platformDocument.setVersionId(event.getPlatformVersionId());
        platformDocument.setValuedProperties(valuedProperties);

        platformDocument.extractInstancePropertiesAndSave(platformRepository);
    }

    /*** QUERY HANDLERS ***/

    @QueryHandler
    @Override
    public Optional<String> onGetPlatformIdFromKeyQuery(GetPlatformIdFromKeyQuery query) {
        PlatformKeyDocument keyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository
                .findOptionalIdByKey(keyDocument)
                .map(PlatformDocument::getId);
    }

    @QueryHandler
    @Override
    public Optional<PlatformView> onGetPlatformByIdQuery(GetPlatformByIdQuery query) {
        return Optional.ofNullable(platformRepository.findOne(query.getPlatformId()))
                .map(PlatformDocument::toPlatformView);
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
    public Boolean onPlatformExistsByKeyQuery(PlatformExistsByKeyQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.countByKey(platformKeyDocument) > 0;
    }

    @QueryHandler
    @Override
    public Optional<ApplicationView> onGetApplicationByNameQuery(GetApplicationByNameQuery query) {
        List<PlatformDocument> platformDocuments = platformRepository.findAllByKeyApplicationName(query.getApplicationName());
        return Optional.ofNullable(CollectionUtils.isEmpty(platformDocuments) ? null
                : PlatformDocument.toApplicationView(query.getApplicationName(), platformDocuments));
    }

    @QueryHandler
    @Override
    public List<InstancePropertyView> onGetInstanceModelQuery(GetInstanceModelQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final PlatformDocument platformDocument = platformRepository.findByKeyAndFilterDeployedModulesByPropertiesPath(platformKeyDocument, query.getPath());

        return Optional.ofNullable(platformDocument.getDeployedModules())
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(deployedModule -> deployedModule.getInstanceProperties()
                        .stream()
                        .map(InstancePropertyDocument::toInstancePropertyView))
                .collect(Collectors.toList());
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

        List<PlatformDocument> pd = platformRepository.findAll();

        List<PlatformDocument> platformDocuments =
                platformRepository.findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(
                        query.getApplicationName(),
                        platformName);

        return platformDocuments
                .stream()
                .map(PlatformDocument::toSearchPlatformResultView)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<SearchApplicationResultView> onSearchApplicationsQuery(SearchApplicationsQuery query) {

        List<PlatformDocument> platformDocuments = platformRepository
                .findAllByKeyApplicationNameLike(query.getApplicationName());

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
                .flatMap(deployedModuleDocument -> Optional.ofNullable(deployedModuleDocument.getValuedProperties())
                        .orElse(Collections.emptyList())
                        .stream())
                .collect(Collectors.toList());
        return AbstractValuedPropertyDocument.toAbstractValuedPropertyViews(abstractValuedPropertyDocuments);
    }

    @QueryHandler
    @Override
    public List<ValuedPropertyView> onGetGlobalPropertiesQuery(final GetGlobalPropertiesQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.findOptionalByKey(platformKeyDocument)
                .map(PlatformDocument::getValuedProperties)
                .map(ValuedPropertyDocument::toValuedPropertyViews)
                .orElse(Collections.emptyList());
    }
}
