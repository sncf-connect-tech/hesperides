/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.applications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.storage.RedisEventStore;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

import java.util.Optional;
import java.util.Set;

/**
 * Created by william_montaz on 22/04/2015.
 */
public class SnapshotRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisEventStore.class);

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private final Pool<Jedis> connectionPool;

    public SnapshotRegistry(Pool<Jedis> connectionPool) {
        this.connectionPool = connectionPool;
    }

    void createSnapshot(SnapshotKey key, Object snapshot) {
        try (Jedis jedis = connectionPool.getResource()) {
            jedis.set(key.getIdentifier(), MAPPER.writeValueAsString(snapshot));
        } catch (JsonProcessingException e) {
            LOGGER.error("A problem occured when trying to serialize snapshot object");
            LOGGER.error(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error("UNEXPECTED EXCEPTION WHILE STORING SNAPSHOT {} ", key);
            LOGGER.error(e.getMessage());
        }
    }

    Set<String> getKeys(String pattern) {
        try (Jedis jedis = connectionPool.getResource()) {
            return jedis.keys(pattern);
        }
    }

    <U> Optional<U> getSnapshot(SnapshotKey snapshotKey, Class snapshotClass) {
        try (Jedis jedis = connectionPool.getResource()) {

            String objectAsString = jedis.get(snapshotKey.getIdentifier());
            return Optional.of((U) MAPPER.readValue(objectAsString, snapshotClass));

        } catch (Exception e) {
            LOGGER.error("A problem occured when trying to get snapshot object");
            LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }

}
