package org.hesperides.infrastructure.mongo.technos;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {

//    @Query("{'templates.name': ?0}")
//    TemplateDocument findTemplate(String templateName);

    //TODO Créer une méthode findByKey
}
