package org.hesperides.infrastructure.mongo.modules;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Profile("mongo")
@Repository
public interface MongoModuleRepository extends MongoRepository<ModuleDocument, String> {
}
