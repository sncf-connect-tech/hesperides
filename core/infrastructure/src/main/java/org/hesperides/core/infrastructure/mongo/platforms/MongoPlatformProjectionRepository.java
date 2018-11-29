package org.hesperides.core.infrastructure.mongo.platforms;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.spring.HasProfile;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.platforms.documents.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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
        PlatformDocument platformDocument = new PlatformDocument(event.getPlatformId(), event.getPlatform());
        platformDocument.extractInstancePropertiesAndSave(platformRepository);
    }

    @EventHandler
    @Override
    public void onPlatformCopiedEvent(PlatformCopiedEvent event) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(event.getExistingPlatformKey());
        PlatformDocument existingPlatform = platformRepository.findOptionalByKey(platformKeyDocument).get();
        PlatformDocument newPlatform = new PlatformDocument(event.getNewPlatformId(), event.getNewPlatform());
        newPlatform.setDeployedModules(existingPlatform.getDeployedModules());
        newPlatform.setValuedProperties(existingPlatform.getValuedProperties());
        platformRepository.save(newPlatform);
    }

    @EventHandler
    @Override
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        platformRepository.deleteById(event.getPlatformId());
    }

    @EventHandler
    @Override
    public void onPlatformUpdatedEvent(PlatformUpdatedEvent event) {
        PlatformDocument platformDocument = new PlatformDocument(event.getPlatformId(), event.getPlatform());
        platformDocument.extractInstancePropertiesAndSave(platformRepository);
    }

    @EventHandler
    @Override
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {

        // Tranformation des propriétés du domaine en documents
        final List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = AbstractValuedPropertyDocument.fromAbstractDomainInstances(event.getValuedProperties());

        // Récupération de la plateforme et mise à jour de la version
        PlatformDocument platformDocument = platformRepository.findById(event.getPlatformId()).get();
        if (HasProfile.dataMigration() && event.getPlatformVersionId() == 0L) {
            // Rustine temporaire pour le temps de la migration
            platformDocument.setVersionId(platformDocument.getVersionId() + 1);
        } else {
            platformDocument.setVersionId(event.getPlatformVersionId());
        }

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

        PlatformDocument platformDocument = platformRepository.findById(event.getPlatformId()).get();

        // Update platform information
        if (HasProfile.dataMigration() && event.getPlatformVersionId() == 0L) {
            // Rustine temporaire pour le temps de la migration
            platformDocument.setVersionId(platformDocument.getVersionId() + 1);
        } else {
            platformDocument.setVersionId(event.getPlatformVersionId());
        }
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
        return platformRepository.findById(query.getPlatformId()).map(PlatformDocument::toPlatformView);
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
        return platformRepository.existsByKey(platformKeyDocument);
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

    @Override
    public Boolean onDeployedModuleExistsQuery(DeployedModuleExistsQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        Module.Key moduleKey = query.getModuleKey();
        return platformRepository.existsByPlatformKeyAndModuleKeyAndPath(
                platformKeyDocument,
                moduleKey.getName(),
                moduleKey.getVersion(),
                moduleKey.isWorkingCopy(),
                query.getModulePath());
    }

    @Override
    public Boolean onInstanceExistsQuery(InstanceExistsQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        Module.Key moduleKey = query.getModuleKey();
        return platformRepository.existsByPlatformKeyAndModuleKeyAndPathAndInstanceName(
                platformKeyDocument,
                moduleKey.getName(),
                moduleKey.getVersion(),
                moduleKey.isWorkingCopy(),
                query.getModulePath(),
                query.getInstanceName());
    }
}
