package org.hesperides.core.infrastructure.mongo.platforms;

import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public interface MongoPlatformRepository extends MongoRepository<PlatformDocument, String> {

    Optional<PlatformDocument> findOptionalByKey(PlatformKeyDocument platformKeyDocument);

    void deleteByKey(PlatformKeyDocument key);

    List<PlatformDocument> findAllByKeyApplicationName(String applicationName);

    List<PlatformDocument> findAllByDeployedModulesNameAndDeployedModulesVersionAndDeployedModulesIsWorkingCopy(
            String moduleName, String moduleVersion, boolean isWorkingCopy);

    List<PlatformDocument> findAllByKeyApplicationNameLike(String input, Pageable pageable);

    List<PlatformDocument> findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(String applicationName, String platformName, Pageable pageable);
}
