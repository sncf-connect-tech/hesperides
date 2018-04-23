package org.hesperides.infrastructure.mongo.technos;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Profile("mongo")
@Repository
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {

//    @Query("{'templates.name': ?0}")
//    TemplateDocument findTemplate(String templateName);
}
