package org.hesperides.infrastructure.mongo.modules;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoModuleRepository extends MongoRepository<ModuleDocument, String> {
}
