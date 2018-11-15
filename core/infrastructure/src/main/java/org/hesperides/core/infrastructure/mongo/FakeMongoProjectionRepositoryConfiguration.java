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
package org.hesperides.core.infrastructure.mongo;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.net.InetSocketAddress;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;

@Configuration
@Profile(FAKE_MONGO)
public class FakeMongoProjectionRepositoryConfiguration {

    private static final String MONGO_DB_NAME = "fake_database";

    @Bean(destroyMethod = "close")
    public MongoClient mongo() {
        final MongoServer server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        final InetSocketAddress serverAddress = server.bind();

        return new MongoClient(new ServerAddress(serverAddress));
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongo(), MONGO_DB_NAME);
    }

}
