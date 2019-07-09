package org.hesperides.core.infrastructure.mongo.platforms;

import org.hesperides.core.infrastructure.MinimalPlatformRepository;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public interface MongoPlatformRepository extends MongoRepository<PlatformDocument, String>, MinimalPlatformRepository {

    @Query(value = "{ 'key' : ?0 }", fields = "{ '_id' : 1 }")
    Optional<PlatformDocument> findOptionalIdByKey(PlatformKeyDocument key);

    boolean existsByKey(PlatformKeyDocument platformKeyDocument);

    Optional<PlatformDocument> findOptionalByKey(PlatformKeyDocument platformKeyDocument);

    List<PlatformDocument> findAllByKeyApplicationName(String applicationName);

    List<PlatformDocument> findAllByDeployedModulesNameAndDeployedModulesVersionAndDeployedModulesIsWorkingCopy(
            String moduleName, String moduleVersion, boolean isWorkingCopy);

    @Query(value = "{}", fields = "{ 'key.applicationName' : 1 }")
    List<PlatformDocument> listApplicationNames();

    @Query(value = "{ 'key.applicationName': { '$regex' : ?0, '$options' : 'i' } }")
        // case-insensitive
    List<PlatformDocument> findAllByKeyApplicationNameLike(String input);

    @Query(value = "{ 'key.applicationName': { '$regex' : ?0, '$options' : 'i' }, 'key.platformName': { '$regex' : ?1, '$options' : 'i' } }")
        // case-insensitive
    List<PlatformDocument> findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(String applicationName, String platformName);

    // Pour les 3 prochaines requêtes, nous utilisons `$gt: 0` car `$ne: 0` ne retournait pas de résultat
    @Query(value = "{ 'key': ?0 }", fields = "{ 'deployedModules' : { $elemMatch : { 'id' : { $gt: 0 }, 'propertiesPath' : ?1 }}}")
    Optional<PlatformDocument> findModuleByPropertiesPath(PlatformKeyDocument platformKeyDocument, String propertiesPath);

    @ExistsQuery("{ 'key' : ?0, 'deployedModules.id' : { $gt: 0 }, 'deployedModules.name' : ?1, 'deployedModules.version' : ?2, 'deployedModules.isWorkingCopy' : ?3, 'deployedModules.modulePath' : ?4}")
    boolean existsByPlatformKeyAndModuleKeyAndPath(PlatformKeyDocument platformKeyDocument, String moduleName, String moduleVersion, boolean isWorkingCopy, String modulePath);

    @ExistsQuery(value = "{ 'key' : ?0, 'deployedModules.id' : { $gt: 0 }, 'deployedModules.name' : ?1, 'deployedModules.version' : ?2, 'deployedModules.isWorkingCopy' : ?3, 'deployedModules.modulePath' : ?4, 'deployedModules.instances.name' : ?5}")
    boolean existsByPlatformKeyAndModuleKeyAndPathAndInstanceName(PlatformKeyDocument platformKeyDocument, String moduleName, String moduleVersion, boolean isWorkingCopy, String modulePath, String instanceName);

    @Query(value = "{ 'key' : ?0 }", fields = "{ 'globalProperties' : 1 }")
    Optional<PlatformDocument> findGlobalPropertiesByPlatformKey(PlatformKeyDocument platformKeyDocument);

    boolean existsByKeyApplicationName(String applicationName);
}
