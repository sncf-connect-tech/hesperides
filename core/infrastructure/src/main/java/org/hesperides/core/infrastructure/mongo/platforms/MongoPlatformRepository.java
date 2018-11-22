package org.hesperides.core.infrastructure.mongo.platforms;

import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public interface MongoPlatformRepository extends MongoRepository<PlatformDocument, String> {

    @Query(value = "{ 'key' : ?0 }", fields = "{ '_id' : 1 }")
    Optional<PlatformDocument> findOptionalIdByKey(PlatformKeyDocument key);

    boolean existsByKey(PlatformKeyDocument platformKeyDocument);

    Optional<PlatformDocument> findOptionalByKey(PlatformKeyDocument platformKeyDocument);

    List<PlatformDocument> findAllByKeyApplicationName(String applicationName);

    List<PlatformDocument> findAllByDeployedModulesNameAndDeployedModulesVersionAndDeployedModulesIsWorkingCopy(
            String moduleName, String moduleVersion, boolean isWorkingCopy);

    List<PlatformDocument> findAllByKeyApplicationNameLike(String input);

    List<PlatformDocument> findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(String applicationName, String platformName);

    @Query(value = "{'key': ?0}", fields = "{ 'deployedModules' : { $elemMatch : { 'propertiesPath' : ?1}}}")
    PlatformDocument findByKeyAndFilterDeployedModulesByPropertiesPath(PlatformKeyDocument platformKeyDocument, String path);

    @ExistsQuery("{'key': ?0, 'deployedModules.name': ?1, 'deployedModules.version': ?2, 'deployedModules.isWorkingCopy': ?3, 'deployedModules.path': ?4}")
    boolean existsByPlatformKeyAndModuleKeyAndPath(PlatformKeyDocument platformKeyDocument, String moduleName, String moduleVersion, boolean isWorkingCopy, String modulePath);

    @ExistsQuery(value = "{'key': ?0, 'deployedModules.name': ?1, 'deployedModules.version': ?2, 'deployedModules.isWorkingCopy': ?3, 'deployedModules.path': ?4, 'deployedModules.instances.name': ?5}")
    boolean existsByPlatformKeyAndModuleKeyAndPathAndInstanceName(PlatformKeyDocument platformKeyDocument, String moduleName, String moduleVersion, boolean isWorkingCopy, String modulePath, String instanceName);
}
