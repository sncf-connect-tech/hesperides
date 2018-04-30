package org.hesperides.infrastructure.mongo.modules.queries;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.domain.modules.*;
import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.queries.ModuleQueriesRepository;
import org.hesperides.domain.modules.queries.ModuleView;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;
import org.hesperides.infrastructure.mongo.modules.ModuleDocument;
import org.hesperides.infrastructure.mongo.modules.MongoModuleRepository;
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

    private final MongoModuleRepository repository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoModuleQueriesRepository(MongoModuleRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @QueryHandler
    @Override
    public Optional<ModuleView> query(GetModuleByKeyQuery query) {
        Optional<ModuleView> moduleView = Optional.empty();
        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        if (moduleDocument != null) {
            moduleView = Optional.of(moduleDocument.toModuleView());
        }
        return moduleView;
    }

    @QueryHandler
    @Override
    public List<String> query(GetModulesNamesQuery query) {
        return mongoTemplate.getCollection("module").distinct("name");
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleTypesQuery query) {
        return repository.findByNameAndVersion(query.getModuleName(), query.getModuleVersion())
                .stream()
                .map(ModuleDocument::isWorkingCopy)
                .map(isWorkingCopy -> Module.Type.toString(isWorkingCopy))
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public List<String> query(GetModuleVersionsQuery query) {
        return repository.findByName(query.getModuleName())
                .stream()
                .map(ModuleDocument::getVersion)
                .collect(Collectors.toList());
    }

    @QueryHandler
    @Override
    public Boolean query(ModuleAlreadyExistsQuery query) {
        TemplateContainer.Key key = query.getModuleKey();
        ModuleDocument moduleDocument = repository.findByNameAndVersionAndWorkingCopy(key.getName(), key.getVersion(), key.isWorkingCopy());
        return moduleDocument != null;
    }

    @QueryHandler
    @Override
    public List<ModuleView> query(SearchModulesQuery query) {
        /**
         * On crée une projection pour effectuer une recherche sur la concaténation de deux champs : name et version.
         * 1 : Une projection nécessite de définir les champs qu'on veut récupérer
         * 2 : La concaténation entraine la création d'un champs temporaire qu'on nomme "nameAndVersion"
         * 3 : La recherche est une expression régulière (équivalent de LIKE) appliquée à ce nouveau champs
         * 4 : On limite le nom de résultats
         */
        AggregationOperation project = Aggregation.project("name", "version", "workingCopy") // 1
                .and(StringOperators.Concat.valueOf("name").concat(" ").concatValueOf("version")).as("nameAndVersion"); // 2
        AggregationOperation match = Aggregation.match(Criteria.where("nameAndVersion").regex(query.getInput())); // 3
        AggregationOperation limit = Aggregation.limit(10); // 4

        TypedAggregation<ModuleDocument> aggregation = TypedAggregation.newAggregation(ModuleDocument.class, project, match, limit);
        List<ModuleDocument> modules = mongoTemplate.aggregate(aggregation, ModuleDocument.class).getMappedResults();

        return modules.stream().map(moduleDocument -> moduleDocument.toModuleView()).collect(Collectors.toList());
    }
}
