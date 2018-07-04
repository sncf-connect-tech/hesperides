package org.hesperides.infrastructure.mongo.platforms;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.platforms.*;
import org.hesperides.domain.platforms.queries.views.*;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

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
    public Optional<PlatformView> onGetPlatformByKeyQuery(GetPlatformByKeyQuery query) {
        Optional<PlatformView> optionalPlatformView = Optional.empty();
        PlatformKeyDocument platformKeyDocument = new PlatformKeyDocument(query.getPlatformKey());
        Optional<PlatformDocument> optionalPlatformDocument = platformRepository.findOptionalByKey(platformKeyDocument);
        if (optionalPlatformDocument.isPresent()) {
            optionalPlatformView = Optional.of(optionalPlatformDocument.get().toPlatformView());
        }
        return optionalPlatformView;
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
}
