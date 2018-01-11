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
package com.vsct.dt.hesperides.infrastructure.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RedisClient {

    private final RedisConfiguration redisConfiguration;

    @Inject
    public RedisClient(final RedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
    }

    public Jedis getJedis() {
        return new Jedis(
                redisConfiguration.getHost(),
                redisConfiguration.getPort(),
                redisConfiguration.getTimeout());
    }

    /**
     * Get all the keys matching the pattern, using a cursor and ScanParam.
     * Before, we used jedis.keys(pattern) which is not adapted to massive stores.
     * See https://stackoverflow.com/questions/33842026/how-to-use-scan-commands-in-jedis
     * and https://stackoverflow.com/questions/13135573/extracting-keys-from-redis
     *
     * @param pattern
     * @return
     */
    public List<String> getKeys(final String pattern) {
        return (List<String>) getJedis().keys(pattern);
//        List<String> result = new ArrayList<>();
//        // https://redis.io/commands/scan#the-count-option
//        ScanParams scanParams = new ScanParams().count(100).match(pattern);
//        String cursor = ScanParams.SCAN_POINTER_START;
//        do {
//            ScanResult<String> scanResult = getJedis().scan(cursor, scanParams);
//            result.addAll(scanResult.getResult());
//            cursor = scanResult.getStringCursor();
//        } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
//        return result;
    }

}
