package org.hesperides.infrastructure.mongo.technos;

import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.templatecontainer.TemplateDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Profile("mongo")
@Repository
public interface MongoTechnoRepository extends MongoRepository<TechnoDocument, String> {

    @Query("{'templates.name': ?0}")
    TemplateDocument findTemplate(String templateName);
}
