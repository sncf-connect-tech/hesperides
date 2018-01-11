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
package com.vsct.dt.hesperides.bdd;

import ai.grakn.redismock.RedisServer;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import redis.clients.jedis.Jedis;

import java.io.IOException;

public class Hooks {
    private RedisServer server;
    public static Jedis jedis;

    @Before
    public void before() throws IOException {
        System.out.println("\nBEFORE");
        server = new RedisServer(Conf.REDIS_PORT);
        server.start();
        jedis = new Jedis(Conf.REDIS_HOST, Conf.REDIS_PORT);
    }

    @After
    public void after() {
        System.out.println("\nAFTER");
        if (jedis != null && jedis.isConnected()) {
            jedis.disconnect();
        }
        if (server != null) {
            server.stop();
        }
    }
}
