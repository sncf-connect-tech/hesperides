package org.hesperides.infrastructure.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

import static org.hesperides.domain.Profiles.MONGO;

@Configuration
@EnableTransactionManagement
@EnableMongoRepositories(basePackages = "org.hesperides.infrastructure.mongo")
@Import({MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@Profile(MONGO)
public class MongoConfiguration {
    @Value("${hesperides.viewstore.database}")
    private String database;

    @Value("${hesperides.viewstore.host}")
    private String host;

    @Value("${hesperides.viewstore.port}")
    private int port;

    @Value("${hesperides.viewstore.username}")
    private String username;

    @Value("${hesperides.viewstore.password}")
    private String password;

    @Bean
    public Mongo mongo() throws Exception {
        return new MongoClient(new ServerAddress(host, port), Collections.singletonList(MongoCredential.createCredential(username, database, password.toCharArray())));
    }
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), database);
    }



}
