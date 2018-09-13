package org.hesperides.core.infrastructure.mongo.platforms;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.platforms.GetApplicationByNameQuery;
import org.hesperides.core.domain.platforms.GetGlobalPropertiesQuery;
import org.hesperides.core.domain.platforms.GetInstanceModelQuery;
import org.hesperides.core.domain.platforms.GetPlatformByKeyQuery;
import org.hesperides.core.domain.platforms.GetPlatformsUsingModuleQuery;
import org.hesperides.core.domain.platforms.GetDeployedModulesPropertiesQuery;
import org.hesperides.core.domain.platforms.PlatformCreatedEvent;
import org.hesperides.core.domain.platforms.PlatformDeletedEvent;
import org.hesperides.core.domain.platforms.PlatformExistsByKeyQuery;
import org.hesperides.core.domain.platforms.PlatformProjectionRepository;
import org.hesperides.core.domain.platforms.PlatformUpdatedEvent;
import org.hesperides.core.domain.platforms.SearchApplicationsQuery;
import org.hesperides.core.domain.platforms.SearchPlatformsQuery;
import org.hesperides.core.domain.platforms.queries.views.ApplicationView;
import org.hesperides.core.domain.platforms.queries.views.InstanceModelView;
import org.hesperides.core.domain.platforms.queries.views.ModulePlatformView;
import org.hesperides.core.domain.platforms.queries.views.PlatformView;
import org.hesperides.core.domain.platforms.queries.views.SearchApplicationResultView;
import org.hesperides.core.domain.platforms.queries.views.SearchPlatformResultView;
import org.hesperides.core.domain.platforms.queries.views.properties.AbstractValuedPropertyView;
import org.hesperides.core.domain.platforms.queries.views.properties.ValuedPropertyView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.core.infrastructure.mongo.platforms.documents.AbstractValuedPropertyDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.InstanceDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.ValuedPropertyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
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
        return  platformDocument.getDeployedModules().stream()
                .findFirst().flatMap(deployedModuleDocument -> deployedModuleDocument.getInstances().stream().findFirst())
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
