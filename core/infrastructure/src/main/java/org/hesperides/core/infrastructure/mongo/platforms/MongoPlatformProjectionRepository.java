package org.hesperides.core.infrastructure.mongo.platforms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.spring.HasProfile;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.exceptions.PlatformNotFoundException;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.platforms.documents.AbstractValuedPropertyDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.ValuedPropertyDocument;
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

@Slf4j
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
        platformDocument.buildInstancesModelAndSave(platformRepository);
    }

    @EventHandler
    @Override
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        platformRepository.deleteById(event.getPlatformId());
    }

    @EventHandler
    @Override
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
        Optional<PlatformDocument> existingPlatformDocument = platformRepository.findById(event.getPlatformId());
        if (!existingPlatformDocument.isPresent()) {
            throw new PlatformNotFoundException("Platform not found - update impossible - platformId: " + event.getPlatformId());
        }
        PlatformDocument platformDocument = existingPlatformDocument.get();
        platformDocument.fillExistingAndUpgradedModulesWithProperties(newPlatformDocument.getDeployedModules(), event.getCopyPropertiesForUpgradedModules());
        if (HasProfile.dataMigration() && newPlatformDocument.getVersionId() == 0L) {
            // Rustine temporaire pour le temps de la migration
            platformDocument.setVersionId(platformDocument.getVersionId() + 1);
        } else {
            platformDocument.setVersionId(newPlatformDocument.getVersionId());
        }
        platformDocument.buildInstancesModelAndSave(platformRepository);
    }

    @EventHandler
    @Override
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {

        // Tranformation des propriétés du domaine en documents
        final List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = AbstractValuedPropertyDocument.fromAbstractDomainInstances(event.getValuedProperties());

        // Récupération de la plateforme et mise à jour de la version
        Optional<PlatformDocument> optPlatformDocument = platformRepository.findById(event.getPlatformId());
        if (!optPlatformDocument.isPresent()) {
            if (HasProfile.dataMigration()) {
                // Cette rustine permet de gérer le cas de VSL-PRD1-BETA par exemple,
                // où les 10 derniers évènements connus entrainent des MAJ de cette platforme supprimée
                log.warn("PlatformModulePropertiesUpdatedEvent event received but platform does not exist: {}", event.getPlatformId());
                return;
            } else {
                throw new NotFoundException("Platform not found - module properties update impossible - platform ID: " + event.getPlatformId());
            }
        }
        PlatformDocument platformDocument = optPlatformDocument.get();
        if (HasProfile.dataMigration() && event.getPlatformVersionId() == 0L) {
            // Rustine temporaire pour le temps de la migration
            platformDocument.setVersionId(platformDocument.getVersionId() + 1);
        } else {
            platformDocument.setVersionId(event.getPlatformVersionId());
        }

        // Modification des propriétés du module dans la plateforme
        platformDocument.getDeployedModules().stream()
                .filter(moduleDocument -> moduleDocument.getPropertiesPath().equals(event.getPropertiesPath()))
                .findAny().ifPresent(module -> {
            module.setValuedProperties(abstractValuedPropertyDocuments);
        });
        platformDocument.buildInstancesModelAndSave(platformRepository);
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

        Optional<PlatformDocument> optPlatformDocument = platformRepository.findById(event.getPlatformId());
        if (!optPlatformDocument.isPresent()) {
            if (HasProfile.dataMigration()) {
                // Cette rustine permet de gérer le cas de VSL-PRD1-BETA par exemple,
                // où les 10 derniers évènements connus entrainent des MAJ de cette platforme supprimée
                log.warn("PlatformPropertiesUpdatedEvent event received but platform does not exist: {}", event.getPlatformId());
                return;
            } else {
                throw new NotFoundException("Platform not found - platform properties update impossible - platform ID: " + event.getPlatformId());
            }
        }
        PlatformDocument platformDocument = optPlatformDocument.get();

        // Update platform information
        if (HasProfile.dataMigration() && event.getPlatformVersionId() == 0L) {
            // Rustine temporaire pour le temps de la migration
            platformDocument.setVersionId(platformDocument.getVersionId() + 1);
        } else {
            platformDocument.setVersionId(event.getPlatformVersionId());
        }
        platformDocument.setGlobalProperties(valuedProperties);
        platformDocument.buildInstancesModelAndSave(platformRepository);
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
        final Optional<PlatformDocument> platformDocument = platformRepository.findModulePropertiesByPropertiesPath(platformKeyDocument, query.getPropertiesPath());

        final List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = platformDocument
                .map(PlatformDocument::getDeployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .filter(deployedModuleDocument -> query.getPropertiesPath().equals(deployedModuleDocument.getPropertiesPath()))
                .flatMap(deployedModuleDocument -> Optional.ofNullable(deployedModuleDocument.getValuedProperties())
                        .orElse(Collections.emptyList())
                        .stream())
                .collect(Collectors.toList());

        return AbstractValuedPropertyDocument.toAbstractValuedPropertyViews(abstractValuedPropertyDocuments);
    }

    @QueryHandler
    @Override
    public List<String> onGetInstancesModelQuery(GetInstancesModelQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final Optional<PlatformDocument> platformDocument = platformRepository.findModuleInstancesModelByPropertiesPath(platformKeyDocument, query.getPropertiesPath());

        return platformDocument
                .map(PlatformDocument::getDeployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .filter(deployedModuleDocument -> query.getPropertiesPath().equals(deployedModuleDocument.getPropertiesPath()))
                .flatMap(deployedModuleDocument -> Optional.ofNullable(deployedModuleDocument.getInstancesModel())
                        .orElse(Collections.emptyList())
                        .stream())
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<ValuedPropertyView> onGetGlobalPropertiesQuery(final GetGlobalPropertiesQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.findOptionalByKey(platformKeyDocument)
                .map(PlatformDocument::getGlobalProperties)
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
