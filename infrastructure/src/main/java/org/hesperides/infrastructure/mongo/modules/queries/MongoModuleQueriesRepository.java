package org.hesperides.infrastructure.mongo.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
import org.hesperides.infrastructure.mongo.templatecontainer.KeyDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.StringOperators;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hesperides.domain.Profiles.*;

@Profile({MONGO, EMBEDDED_MONGO, FAKE_MONGO})
@Repository
public class MongoModuleQueriesRepository implements ModuleQueriesRepository {

    private final MongoModuleRepository moduleRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoModuleQueriesRepository(MongoModuleRepository moduleRepository, MongoTemplate mongoTemplate) {
        this.moduleRepository = moduleRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> moduleView = Optional.empty();
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(query.getModuleKey()));
        if (moduleDocument != null) {
            moduleView = Optional.of(moduleDocument.toModuleView());
        }
        return moduleView;
    }

    @QueryHandler
    @Override
    public List<String> query(GetModulesNamesQuery query) {
        return mongoTemplate.getCollection("module").distinct("_id.name");
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionTypesQuery query) {
        return moduleRepository.findByKeyNameAndKeyVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::isWorkingCopy)
                .map(TemplateContainer.VersionType::toString)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionsQuery query) {
        return moduleRepository.findByKeyName(query.getModuleName())
                .stream()
                .map(ModuleDocument::getKey)
                .map(KeyDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = moduleRepository.findByKey(KeyDocument.fromDomainInstance(key));
        return moduleDocument != null;
    }

    @QueryHandler
    @Override
    public List<ModuleView> query(SearchModulesQuery query) {
//        Query query1 = new Query();
//        query1.addCriteria(Criteria.where("key.name").regex(query.getInput()));
//        List<ModuleDocument> moduleDocuments = mongoTemplate.find(query, ModuleDocument.class);

        /*
          On crée une projection pour effectuer une recherche sur la concaténation de deux champs : name et version.
          1 : Une projection nécessite de définir les champs qu'on veut récupérer
          2 : La concaténation entraine la création d'un champs temporaire qu'on nomme "nameAndVersion"
          3 : La recherche est une expression régulière (équivalent de LIKE) appliquée à ce nouveau champs
          4 : On limite le nombre de résultats
         */
        AggregationOperation project = Aggregation.project("key") // 1
                .and(StringOperators.Concat.valueOf("key.name").concat(" ").concatValueOf("key.version")).as("nameAndVersion"); // 2
        AggregationOperation match = Aggregation.match(Criteria.where("nameAndVersion").regex(query.getInput())); // 3
        AggregationOperation limit = Aggregation.limit(10); // 4


        TypedAggregation<ModuleDocument> aggregation = TypedAggregation.newAggregation(ModuleDocument.class, project, match, limit);
        List<ModuleDocument> modules = mongoTemplate.aggregate(aggregation, ModuleDocument.class).getMappedResults();

        return modules.stream().map(ModuleDocument::toModuleView).collect(Collectors.toList());
    }
}
