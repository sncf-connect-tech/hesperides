/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.infrastructure.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.UUID;

import static org.hesperides.domain.framework.Profiles.FAKE_MONGO;

@Configuration
@Profile(FAKE_MONGO)
public class FakeMongoProjectionRepositoryConfiguration {

    private static final String MONGO_DB_NAME = "fake_database";

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongo(), MONGO_DB_NAME);
    }

    @Bean(destroyMethod = "close")
    public Mongo mongo() {
        return new Fongo(UUID.randomUUID().toString()).getMongo();
    }
}
