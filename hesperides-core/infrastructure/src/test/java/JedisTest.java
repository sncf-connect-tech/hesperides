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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import static org.assertj.core.api.Assertions.assertThat;

public class JedisTest {

    private static RedisServer server = null;

    @Before
    public void before() {
        server = RedisServer.builder().port(1234).build();
        server.start();
    }

    @Test
    public void test() {
        Jedis jedis = new Jedis("localhost", 1234);
        jedis.rpush("foo", "bar");
        assertThat(jedis.keys("*").size()).isEqualTo(1);
    }

    @After
    public void after() {
        server.stop();
        server = null;
    }
}
