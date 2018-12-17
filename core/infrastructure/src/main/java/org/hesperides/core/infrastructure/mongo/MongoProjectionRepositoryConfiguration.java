package org.hesperides.core.infrastructure.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.validation.constraints.NotNull;

import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Configuration
@Profile(MONGO)
@EnableTransactionManagement
@EnableMongoRepositories(basePackages = "org.hesperides.core.infrastructure.mongo")
@ConfigurationProperties("projection-repository")
public class MongoProjectionRepositoryConfiguration {

    // On expose les noms des méthodes sous formes de strings pour pouvoir les utiliser comme "bean qualifiers" dans datamigration
    public final static String MONGO_CLIENT_URI_BEAN_NAME = "projectionMongoClientUri";
    public final static String MONGO_CLIENT_BEAN_NAME = "projectionMongoClient";
    public final static String MONGO_TEMPLATE_BEAN_NAME = "projectionMongoTemplate";

    @Setter
    @NotNull
    private String uri;

    @Bean
    public MongoClientURI projectionMongoClientUri() {
        return new MongoClientURI(uri);
    }

    @Bean
    @Primary // mongoDbFactory in org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration require it in datamigration
    public MongoClient projectionMongoClient(MongoClientURI projectionMongoClientUri) {
        return new MongoClient(projectionMongoClientUri);
    }

    @Bean({MONGO_TEMPLATE_BEAN_NAME, "mongoTemplate"}) // un Bean nommé mongoTemplate est requis pour les repos SpringData
    public MongoTemplate projectionMongoTemplate(MongoClient projectionMongoClient, MongoClientURI projectionMongoClientUri) {
        return new MongoTemplate(projectionMongoClient, projectionMongoClientUri.getDatabase());
    }
}
