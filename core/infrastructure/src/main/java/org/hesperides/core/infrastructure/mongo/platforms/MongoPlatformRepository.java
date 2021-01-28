package org.hesperides.core.infrastructure.mongo.platforms;

import org.hesperides.core.infrastructure.MinimalPlatformRepository;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformDocument;
import org.hesperides.core.infrastructure.mongo.platforms.documents.PlatformKeyDocument;
import org.springframework.context.annotation.Profile;
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

    @Query(value = "{ 'key': ?0 }", fields = "{ '_id': 1 }")
    Optional<PlatformDocument> findOptionalIdByKey(PlatformKeyDocument key);

    boolean existsByKey(PlatformKeyDocument platformKeyDocument);

    Optional<PlatformDocument> findOptionalByKey(PlatformKeyDocument platformKeyDocument);

    List<PlatformDocument> findAllByKeyApplicationName(String applicationName);

    @Query(value = "{ 'key.applicationName': ?0 }", fields = "{ 'deployedModules': 0 }")
    List<PlatformDocument> findPlatformsForApplicationAndExcludeModules(String applicationName);

    @Query(value = "{ 'deployedModules': { $elemMatch: { 'name': ?0, 'version': ?1, 'isWorkingCopy': ?2, 'id': { $gt: 0 } } } }", fields = "{ 'key': 1 }")
    List<PlatformDocument> findPlatformsUsingModule(String moduleName, String moduleVersion, boolean isWorkingCopy);

    @Query(value = "{}", fields = "{ 'key.applicationName': 1 }")
    List<PlatformDocument> listApplicationNames();

    // case-insensitive
    @Query(value = "{ 'key.applicationName': { '$regex': ?0, '$options': 'i' } }")
    List<PlatformDocument> findAllByKeyApplicationNameLike(String input);

    // case-insensitive
    @Query(value = "{ 'key.applicationName': { '$regex': ?0, '$options': 'i' }, 'key.platformName': { '$regex': ?1, '$options': 'i' } }")
    List<PlatformDocument> findAllByKeyApplicationNameLikeAndKeyPlatformNameLike(String applicationName, String platformName);

    // `$ne` et `$gt` ne sont pas pris en compte
    @Query(value = "{ 'key': ?0 }", fields = "{ 'deployedModules': { $elemMatch: { 'id': { $gt: 0 }, 'propertiesPath': ?1 } } }")
    Optional<PlatformDocument> findModuleByPropertiesPath(PlatformKeyDocument platformKeyDocument, String propertiesPath);

    // issue-767: `$gt: 0` n'est pas pris en compte dans cette requête, à voir avec la nouvelle version de Mongo
//    @ExistsQuery("{ 'key': ?0, 'deployedModules.id': { $gt: 0 }, 'deployedModules.name': ?1, 'deployedModules.version': ?2, 'deployedModules.isWorkingCopy': ?3, 'deployedModules.modulePath': ?4, 'deployedModules.instances.name': ?5}")
//    boolean existsByPlatformKeyAndModuleKeyAndPathAndInstanceName(PlatformKeyDocument platformKeyDocument, String moduleName, String moduleVersion, boolean isWorkingCopy, String modulePath, String instanceName);

    @Query(value = "{ 'key': ?0 }", fields = "{ 'globalProperties': 1 }")
    Optional<PlatformDocument> findGlobalPropertiesByPlatformKey(PlatformKeyDocument platformKeyDocument);

    boolean existsByKeyApplicationName(String applicationName);

    boolean existsByIdAndIsProductionPlatform(String platformId, boolean isProductionPlatform);

    @Query(value = "{ }", fields = "{ 'key': 1, 'isProductionPlatform': 1, 'deployedModules.id': 1, 'deployedModules.propertiesPath': 1, 'deployedModules.valuedProperties': 1 }")
    List<PlatformDocument> findAllApplicationsPropertiesQuery();

    @Query(
            value = "{ " +
                    "   'deployedModules.valuedProperties': {" +
                    "       $elemMatch: {" +
                    "           'name': { '$regex': ?0, '$options': 'i' }," +
                    "           'value': { '$regex': ?1, '$options': 'i' }" +
                    "       }" +
                    "   } " +
                    "}",
            fields = "{" +
                    "   'key': 1," +
                    "   'isProductionPlatform': 1," +
                    "   'deployedModules.id': 1," +
                    "   'deployedModules.propertiesPath': 1," +
                    "   'deployedModules.valuedProperties': 1, " +
                    "}"
    )
    List<PlatformDocument> findPlatformsByPropertiesNameAndValue(String propertyName, String propertyValue);
}
