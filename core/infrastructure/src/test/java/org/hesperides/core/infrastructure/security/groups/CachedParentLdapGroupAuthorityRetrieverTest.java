package org.hesperides.core.infrastructure.security.groups;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.hesperides.core.infrastructure.security.groups.CachedParentLdapGroupAuthorityRetriever.MAX_RECURSION;
import static org.junit.Assert.assertEquals;

public class CachedParentLdapGroupAuthorityRetrieverTest {

    static final String TEST_GROUPS_TREE_CACHE_NAME = "test-authorization-groups-tree";
    static CacheManager cacheManager = CacheManager.newInstance();

    Map<String, HashSet<String>> parentGroupsTree; // groupDN -> parentGroupDN
    Cache cache;
    CachedParentLdapGroupAuthorityRetriever cachedParentLdapGroupAuthorityRetriever;

    @Before
    public void setUp() {
        parentGroupsTree = new HashMap<>();
        cache = new Cache(new CacheConfiguration(TEST_GROUPS_TREE_CACHE_NAME, 5000)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .timeToLiveSeconds(2)
                .diskPersistent(false)
                .eternal(false)
                .overflowToDisk(false));
        if (cacheManager.cacheExists(TEST_GROUPS_TREE_CACHE_NAME)) {
            cacheManager.removeCache(TEST_GROUPS_TREE_CACHE_NAME);
        }
        cacheManager.addCache(cache);
        cachedParentLdapGroupAuthorityRetriever = new CachedParentLdapGroupAuthorityRetriever(cache);
        cachedParentLdapGroupAuthorityRetriever.setParentGroupsDNRetriever(dn ->
                parentGroupsTree.getOrDefault(dn, new HashSet<>())
        );
    }

    @Test
    public void testWithFailingFallback() throws InterruptedException {
        String dummyGroupDN = "CN=DUMMY";
        Set<LdapGroupAuthority> expected = Collections.singleton(new LdapGroupAuthority(dummyGroupDN, 1));

        assertEquals(expected, cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(dummyGroupDN));
        assertEquals(0, cache.getStatistics().cacheHitCount());
        assertEquals(1, cache.getStatistics().cacheMissCount());

        assertEquals(expected, cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(dummyGroupDN));
        assertEquals(1, cache.getStatistics().cacheHitCount());
        assertEquals(1, cache.getStatistics().cacheMissCount());

        Thread.sleep(3000); // we wait until TTL expires

        assertEquals(expected, cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(dummyGroupDN));
        assertEquals(1, cache.getStatistics().cacheHitCount());
        assertEquals(2, cache.getStatistics().cacheMissCount());
        assertEquals(1, cache.getStatistics().cacheMissExpiredCount());
    }

    @Test
    public void testWithSingleParent() {
        String dummyGroupDN = "CN=DUMMY";
        String parentGroupDN = "CN=PARENT";
        parentGroupsTree.put(dummyGroupDN, new HashSet<>(Arrays.asList(parentGroupDN)));

        Set<LdapGroupAuthority> expected = new HashSet<>(Arrays.asList(
                new LdapGroupAuthority(dummyGroupDN, 1),
                new LdapGroupAuthority(parentGroupDN, 2)
        ));

        assertEquals(expected, cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(dummyGroupDN));
    }

    @Test
    public void testWithTwoParents() {
        String dummyGroupDN = "CN=DUMMY";
        String parentGroupDN1 = "CN=PARENT1";
        String parentGroupDN2 = "CN=PARENT2";
        parentGroupsTree.put(dummyGroupDN, new HashSet<>(Arrays.asList(parentGroupDN1, parentGroupDN2)));

        Set<LdapGroupAuthority> expected = new HashSet<>(Arrays.asList(
                new LdapGroupAuthority(dummyGroupDN, 1),
                new LdapGroupAuthority(parentGroupDN1, 2),
                new LdapGroupAuthority(parentGroupDN2, 2)
        ));

        assertEquals(expected, cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(dummyGroupDN));
    }


    @Test(expected = ParentGroupRecursionException.class)
    public void testParentGroupRecursionException() {
        final String groupDNBase = "CN=DUMMY_";
        IntStream.range(0, MAX_RECURSION + 1).forEach(i ->
                parentGroupsTree.put(groupDNBase + i, new HashSet<>(Arrays.asList(groupDNBase + (i + 1)))));
        cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(groupDNBase + 0);
    }
}