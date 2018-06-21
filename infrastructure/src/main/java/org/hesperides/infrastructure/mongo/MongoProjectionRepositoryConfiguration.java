package org.hesperides.infrastructure.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;

import static org.hesperides.domain.framework.Profiles.MONGO;

@Configuration
@Profile(MONGO)
@EnableTransactionManagement
@EnableMongoRepositories(basePackages = "org.hesperides.infrastructure.mongo")
@Getter
@Setter
@Validated
@ConfigurationProperties("projection_repository")
public class MongoProjectionRepositoryConfiguration {

    @NotNull
    private String host;
    @NotNull
    private String port;
    private String database;
    private String username;
    private String password;

    @Bean
    public Mongo projectionRepositoryMongoClient() {
        if (!username.isEmpty()) {
            return new MongoClient(new ServerAddress(host, Integer.parseInt(port)), Collections.singletonList(MongoCredential.createCredential(username, database, password.toCharArray())));
        } else
            return new MongoClient(host, Integer.parseInt(port));
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(projectionRepositoryMongoClient(), database);
    }
}
