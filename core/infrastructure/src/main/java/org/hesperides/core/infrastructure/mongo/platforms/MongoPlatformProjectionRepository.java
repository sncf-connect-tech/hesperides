package org.hesperides.core.infrastructure.mongo.platforms;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.commons.spring.HasProfile;
import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.platforms.*;
import org.hesperides.core.domain.platforms.queries.views.*;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.core.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.core.infrastructure.mongo.platforms.documents.*;
import org.hesperides.core.infrastructure.mongo.templatecontainers.AbstractPropertyDocument;
import org.hesperides.core.infrastructure.mongo.templatecontainers.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.HasProfile.isProfileActive;
import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;
import static org.hesperides.core.infrastructure.Constants.PLATFORM_COLLECTION_NAME;
import static org.hesperides.core.infrastructure.mongo.MongoSearchOptions.ensureCaseInsensitivity;

@Slf4j
@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoPlatformProjectionRepository implements PlatformProjectionRepository {

    private final MongoPlatformRepository platformRepository;
    private final MongoModuleRepository mongoModuleRepository;
    private final MongoTemplate mongoTemplate;
    private final Environment environment;

    @Autowired
    public MongoPlatformProjectionRepository(MongoPlatformRepository platformRepository, MongoModuleRepository mongoModuleRepository,
                                             MongoTemplate mongoTemplate, Environment environment) {
        this.platformRepository = platformRepository;
        this.mongoModuleRepository = mongoModuleRepository;
        this.mongoTemplate = mongoTemplate;
        this.environment = environment;
    }

    @PostConstruct
    private void ensureIndexCaseInsensitivity() {
        if (isProfileActive(environment, MONGO)) {
            ensureCaseInsensitivity(mongoTemplate, PLATFORM_COLLECTION_NAME);
        }
    }

    /*** EVENT HANDLERS ***/

    @EventHandler
    @Override
    public void onPlatformCreatedEvent(PlatformCreatedEvent event) {
//        PlatformDocument platformDocument;
//        if (HasProfile.dataMigration() && platformRepository.existsById(event.getPlatformId())) {
//            platformDocument = platformRepository.findById(event.getPlatformId()).get();
//            platformDocument.setVersionId(1L);
//        } else {
//            platformDocument = new PlatformDocument(event.getPlatformId(), event.getPlatform());
//        }
        PlatformDocument platformDocument = new PlatformDocument(event.getPlatformId(), event.getPlatform());
        platformDocument.buildInstancesModelAndSave(platformRepository);
    }

    @EventHandler
    @Override
    public void onPlatformDeletedEvent(PlatformDeletedEvent event) {
        if (!HasProfile.dataMigration()) {
            platformRepository.deleteById(event.getPlatformId());
        }
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
        platformRepository.findById(event.getPlatformId()).ifPresent(platformDocument -> {
            platformDocument.setVersion(newPlatformDocument.getVersion());
            platformDocument.setProductionPlatform(newPlatformDocument.isProductionPlatform());
            platformDocument.updateModules(newPlatformDocument.getDeployedModules(), event.getCopyPropertiesForUpgradedModules(), newPlatformDocument.getVersionId());

            if (HasProfile.dataMigration() && newPlatformDocument.getVersionId() == 0L) {
                platformDocument.setVersionId(platformDocument.getVersionId() + 1);
            } else {
                platformDocument.setVersionId(newPlatformDocument.getVersionId());
            }

            platformDocument.getDeployedModules()
                    .stream()
                    .filter(deployedModuleDocument -> deployedModuleDocument.getId() > 0)
                    .forEach(deployedModuleDocument -> {
                        completePropertiesWithMustacheContentPasswordAndDefaultValues(deployedModuleDocument.getValuedProperties(), deployedModuleDocument);
                    });
            platformDocument.buildInstancesModelAndSave(platformRepository);
        });
    }

    @EventHandler
    @Override
    public void onPlatformModulePropertiesUpdatedEvent(PlatformModulePropertiesUpdatedEvent event) {
        // Tranformation des propriétés du domaine en documents
        final List<AbstractValuedPropertyDocument> abstractValuedProperties = AbstractValuedPropertyDocument.fromAbstractDomainInstances(event.getValuedProperties());

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
                .filter(deployedModuleDocument -> deployedModuleDocument.getPropertiesPath().equals(event.getPropertiesPath()))
                .findAny().ifPresent(deployedModuleDocument -> {
            completePropertiesWithMustacheContentPasswordAndDefaultValues(abstractValuedProperties, deployedModuleDocument);
        });
        platformDocument.buildInstancesModelAndSave(platformRepository);
    }

    private void completePropertiesWithMustacheContentPasswordAndDefaultValues(List<AbstractValuedPropertyDocument> abstractValuedProperties,
                                                                               DeployedModuleDocument deployedModuleDocument) {
        // Récupérer le model du module afin d'attribuer à chaque
        // propriété valorisée la définition initiale de la propriété
        // (ex: {{prop | @required}} => "prop | @required")
        Module.Key moduleKey = new Module.Key(deployedModuleDocument.getName(), deployedModuleDocument.getVersion(), TemplateContainer.getVersionType(deployedModuleDocument.isWorkingCopy()));
        KeyDocument moduleKeyDocument = new KeyDocument(moduleKey);
        List<AbstractPropertyDocument> moduleProperties = mongoModuleRepository
                .findPropertiesByModuleKey(moduleKeyDocument)
                .map(ModuleDocument::getProperties)
                .orElse(Collections.emptyList());

        List<AbstractValuedPropertyDocument> completedValuedProperties = AbstractValuedPropertyDocument.completePropertiesWithMustacheContentAndIsPassword(abstractValuedProperties, moduleProperties);
        completedValuedProperties.addAll(AbstractValuedPropertyDocument.getUnsetPropertiesWithDefaultValues(completedValuedProperties, moduleProperties));
        deployedModuleDocument.setValuedProperties(completedValuedProperties);
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
    public List<AbstractValuedPropertyView> onGetDeployedModulePropertiesQuery(GetDeployedModulePropertiesQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final Optional<PlatformDocument> platformDocument = platformRepository
                .findModuleByPropertiesPath(platformKeyDocument, query.getPropertiesPath());

        final List<AbstractValuedPropertyDocument> abstractValuedPropertyDocuments = platformDocument
                .map(PlatformDocument::getDeployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(deployedModuleDocument -> Optional.ofNullable(deployedModuleDocument.getValuedProperties())
                        .orElse(Collections.emptyList())
                        .stream())
                .collect(Collectors.toList());

        return AbstractValuedPropertyDocument.toViews(abstractValuedPropertyDocuments);
    }


    @QueryHandler
    @Override
    public List<String> onGetInstancesModelQuery(GetInstancesModelQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        final Optional<PlatformDocument> platformDocument = platformRepository
                .findModuleByPropertiesPath(platformKeyDocument, query.getPropertiesPath());

        return platformDocument
                .map(PlatformDocument::getDeployedModules)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(deployedModuleDocument -> Optional.ofNullable(deployedModuleDocument.getInstancesModel())
                        .orElse(Collections.emptyList())
                        .stream())
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<ValuedPropertyView> onGetGlobalPropertiesQuery(final GetGlobalPropertiesQuery query) {
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        return platformRepository.findGlobalPropertiesByPlatformKey(platformKeyDocument)
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
