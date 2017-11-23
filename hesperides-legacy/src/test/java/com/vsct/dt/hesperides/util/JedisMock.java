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

package com.vsct.dt.hesperides.util;

import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Slowlog;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by emeric_martineau on 02/06/2016.
 */
public class JedisMock implements JedisCommands, MultiKeyCommands, AdvancedJedisCommands, ScriptingCommands, BasicCommands, ClusterCommands, AutoCloseable {
    /**
     * Simulate list in redis.
     */
    private final Map<String, List<String>> redisList = new HashMap<>();

    /**
     * Simulate key/value in redis.
     */
    private final Map<String, String> redisString = new HashMap<>();

    @Override
    public List<String> configGet(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String configSet(String s, String s1) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String slowlogReset() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public Long slowlogLen() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public List<Slowlog> slowlogGet() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public List<Slowlog> slowlogGet(long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long objectRefcount(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String objectEncoding(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public Long objectIdletime(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String ping() {
        return "OK";
    }

    @Override
    public String quit() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String flushDB() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public Long dbSize() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String select(int i) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String flushAll() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String auth(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String save() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String bgsave() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String bgrewriteaof() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public Long lastsave() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String shutdown() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String info() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String info(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String slaveof(String s, int i) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String slaveofNoOne() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public Long getDB() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String debug(DebugParams debugParams) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String configResetStat() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public Long waitReplicas(int i, long l) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterNodes() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterMeet(String s, int i) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterAddSlots(int... ints) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterDelSlots(int... ints) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterInfo() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public List<String> clusterGetKeysInSlot(int i, int i1) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterSetSlotNode(int i, String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterSetSlotMigrating(int i, String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterSetSlotImporting(int i, String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterSetSlotStable(int i) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterForget(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public String clusterFlushSlots() {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }

    @Override
    public Long clusterKeySlot(String s) {
        throw new UnsupportedOperationException("configGet() not implemented.");
        //return null;
    }


    @Override
    public Long clusterCountKeysInSlot(int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String clusterSaveConfig() {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String clusterReplicate(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> clusterSlaves(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String clusterFailover() {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Object> clusterSlots() {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String clusterReset(JedisCluster.Reset reset) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String readonly() {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String set(String redisKey, String s1) {
        this.redisString.put(redisKey, s1);

        return s1;
    }

    @Override
    public String set(String s, String s1, String s2, String s3, long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String set(String key, String value, String nxxx) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String get(String redisKey) {
        return this.redisString.get(redisKey);
    }

    @Override
    public Boolean exists(String redisKey) {
        return this.redisList.containsKey(redisKey) || this.redisString.containsKey(redisKey);
    }

    @Override
    public Long persist(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String type(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long expire(String s, int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long pexpire(final String s, final long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long expireAt(String s, long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long pexpireAt(final String s, final long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long ttl(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long pttl(String key) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Boolean setbit(String s, long l, boolean b) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Boolean setbit(String s, long l, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Boolean getbit(String s, long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long setrange(String s, long l, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getrange(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getSet(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long setnx(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String setex(String s, int i, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long decrBy(String s, long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long decr(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long incrBy(String s, long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Double incrByFloat(String key, double value) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long incr(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long append(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String substr(String s, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long hset(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String hget(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long hsetnx(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String hmset(String s, Map<String, String> map) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> hmget(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long hincrBy(String s, String s1, long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Boolean hexists(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long hdel(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long hlen(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> hkeys(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> hvals(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Map<String, String> hgetAll(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long rpush(String redisKey, String... strings) {
        //return null;
        //throw new UnsupportedOperationException("Not implemented.");

        List<String> currentStream = this.redisList.get(redisKey);

        if (currentStream == null) {
            currentStream = new ArrayList<>();

            this.redisList.put(redisKey, currentStream);
        }

        for (int i = 0; i < strings.length; i++) {
            currentStream.add(strings[i]);
        }

        return (long) currentStream.size();
    }

    @Override
    public Long lpush(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long llen(String redisKey) {
        final List<String> currentStream = this.redisList.get(redisKey);
        int len;

        if (currentStream == null) {
            len = 0;
        } else {
            len = currentStream.size();
        }

        return (long) len;
    }

    @Override
    public List<String> lrange(String redisKey, long start, long stop) {
        final List<String> currentStream = this.redisList.get(redisKey);
        List<String> subList = new ArrayList<>();

        if (currentStream != null) {
            /* Out-of-range indexes
             * Out of range indexes will not produce an error. If start is larger than the end of the list, an empty
             * list is returned. If stop is larger than the actual end of the list, Redis will treat it like the last
             * element of the list.
             */
            if (start < currentStream.size()) {
                if (start < 0) {
                    start = 0;
                }

                if (stop > currentStream.size()) {
                    stop = (long) currentStream.size();
                } else if (stop < 0) {
                    stop = (long) currentStream.size() + 1 + stop;
                } else {
                    stop++;
                }

                try {
                    subList = currentStream.subList((int) start, (int) stop);
                } catch (final IndexOutOfBoundsException e) {
                    System.out.println("Ouille !");
                }
            }
        }

        return subList;
    }

    @Override
    public String ltrim(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String lindex(String s, long l) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String lset(String s, long l, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long lrem(String s, long l, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String lpop(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String rpop(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long sadd(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> smembers(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long srem(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String spop(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> spop(String key, long count) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long scard(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Boolean sismember(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String srandmember(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> srandmember(String s, int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long strlen(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zadd(String s, double v, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zadd(String s, Map<String, Double> map) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrange(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zrem(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Double zincrby(String s, double v, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Double zincrby(String key, double score, String member, ZIncrByParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zrank(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zrevrank(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrevrange(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrangeWithScores(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zcard(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Double zscore(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> sort(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> sort(String s, SortingParams sortingParams) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zcount(String s, double v, double v1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zcount(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrangeByScore(String s, double v, double v1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrangeByScore(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrevrangeByScore(String s, double v, double v1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrangeByScore(String s, double v, double v1, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrevrangeByScore(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrangeByScore(String s, String s1, String s2, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrevrangeByScore(String s, double v, double v1, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, double v, double v1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, double v, double v1, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrevrangeByScore(String s, String s1, String s2, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String s, String s1, String s2, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, double v, double v1, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String s, String s1, String s2, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zremrangeByRank(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zremrangeByScore(String s, double v, double v1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zremrangeByScore(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zlexcount(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrangeByLex(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrangeByLex(String s, String s1, String s2, int i, int i1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zremrangeByLex(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long linsert(String s, BinaryClient.LIST_POSITION list_position, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long lpushx(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long rpushx(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> blpop(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> blpop(int i, String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> brpop(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> brpop(int i, String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long del(String s) {
        long r;

        if (this.redisList.containsKey(s) || this.redisString.containsKey(s)) {
            r = 1L;
        } else {
            r = 0L;
        }

        this.redisList.remove(s);
        this.redisString.remove(s);

        return r;
    }

    @Override
    public String echo(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long move(String s, int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long bitcount(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long bitcount(String s, long l, long l1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long bitpos(String key, boolean value) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long bitpos(String key, boolean value, BitPosParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String s, int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<String> sscan(String s, int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<Tuple> zscan(String s, int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<String> sscan(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<Tuple> zscan(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long pfadd(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public long pfcount(String s) {
        return 0;
    }

    @Override
    public Long geoadd(String key, double longitude, double latitude, String member) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> geohash(String key, String... members) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Long> bitfield(String key, String... arguments) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long del(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long exists(String... keys) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> blpop(int i, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> brpop(int i, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> blpop(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<String> brpop(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> keys(final String patternKey) {
        final List<String> listOfKeys = new ArrayList<>();

        final String searchPattern = Pattern.compile("\\*").matcher(patternKey).replaceAll(".*");

        final Pattern p = Pattern.compile(searchPattern);

        listRedisKeys(this.redisList, listOfKeys, p);
        listRedisKeys(this.redisString, listOfKeys, p);

       return listOfKeys.stream().collect(Collectors.toSet());
    }

    private void listRedisKeys(Map<String, ?> map, List<String> listOfKeys, Pattern p) {
        Matcher m;

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            m = p.matcher(entry.getKey());

            if (m.matches()) {
                listOfKeys.add(entry.getKey());
            }
        }
    }

    @Override
    public List<String> mget(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String mset(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long msetnx(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String rename(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long renamenx(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String rpoplpush(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> sdiff(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long sdiffstore(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> sinter(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long sinterstore(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long smove(String s, String s1, String s2) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long sort(String s, SortingParams sortingParams, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long sort(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Set<String> sunion(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long sunionstore(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String watch(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String unwatch() {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zinterstore(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zinterstore(String s, ZParams zParams, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zunionstore(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long zunionstore(String s, ZParams zParams, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String brpoplpush(String s, String s1, int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long publish(String s, String s1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... strings) {

    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... strings) {

    }

    @Override
    public String randomKey() {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Long bitop(BitOP bitOP, String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<String> scan(int i) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<String> scan(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String pfmerge(String s, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public long pfcount(String... strings) {
        return 0;
    }

    @Override
    public Object eval(String s, int i, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object eval(String s, List<String> list, List<String> list1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object eval(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object evalsha(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object evalsha(String s, List<String> list, List<String> list1) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object evalsha(String s, int i, String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Boolean scriptExists(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Boolean> scriptExists(String... strings) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String scriptLoad(String s) {
        //return null;
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void clear() {
        this.redisList.clear();
        this.redisString.clear();
    }

    @Override
    public void close() throws Exception {
        // Nothing
    }

    public String getLastEvent(String streamName) {
        final List<String> eventList = this.redisList.get(streamName);

        return eventList.get(eventList.size() - 1);
    }
}
