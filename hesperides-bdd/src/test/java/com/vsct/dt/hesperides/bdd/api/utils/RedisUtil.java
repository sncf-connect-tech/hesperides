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
package com.vsct.dt.hesperides.bdd.api.utils;

import ai.grakn.redismock.RedisServer;
import com.vsct.dt.hesperides.bdd.api.Conf;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class RedisUtil {
    private RedisServer redis;

    public RedisUtil() throws IOException {
        redis = new RedisServer(Conf.REDIS_PORT);
    }

    public void startRedis() {
        try {
            redis.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRedis() {
        if (redis != null) {
            redis.stop();
        }
    }
}
