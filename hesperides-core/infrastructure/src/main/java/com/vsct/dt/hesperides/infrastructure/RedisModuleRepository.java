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
package com.vsct.dt.hesperides.infrastructure;


import com.vsct.dt.hesperides.domain.modules.Module;
import com.vsct.dt.hesperides.domain.modules.ModuleRepository;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class RedisModuleRepository implements ModuleRepository {

    private static final String prefix = "module-";

    private final RedisClient redisClient;

    @Inject
    public RedisModuleRepository(final RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public List<Module> getActiveModulesName() {
        List<Module> modules = new ArrayList<>();
        for (String key : redisClient.getKeys(String.format("%s*", prefix))) {
            /**
             * TODO: Pour chaque clé, regarder si le dernier élément est un delete
             * TODO: Mapper la valeur redis vers un objet java
             */
            Jedis jedis = redisClient.getJedis();
            if (jedis.exists(key)) {
                final long lastIndex = jedis.llen(key) - 1;
                final List<String> lastCache = jedis.lrange(key, lastIndex, lastIndex);
                for (String s : lastCache) {
                    System.out.println(s);
                }
            }



            modules.add(new Module(key.replace(prefix, "")));
        }
        return modules;
    }
}
