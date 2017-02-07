/*
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
 */

package com.vsct.dt.hesperides.storage;


import redis.clients.jedis.*;

import java.io.Closeable;

/**
 * Created by emeric_martineau on 30/05/2016.
 */
public interface RedisCommand<T, A extends JedisCommands&MultiKeyCommands&AdvancedJedisCommands&ScriptingCommands&BasicCommands&ClusterCommands&Closeable> {
    T execute(A jedisData, A jedisSnapshot) throws Throwable;
    void error(final Throwable e);
}
