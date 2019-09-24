package org.hesperides.core.infrastructure.security.groups;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
        Set<String> expected = Collections.singleton(dummyGroupDN);

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
        parentGroupsTree.put(dummyGroupDN, new HashSet<>(Collections.singletonList(parentGroupDN)));

        Set<String> expected = new HashSet<>(Arrays.asList(dummyGroupDN, parentGroupDN));
        assertEquals(expected, cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(dummyGroupDN));
    }

    @Test
    public void testWithTwoParents() {
        String dummyGroupDN = "CN=DUMMY";
        String parentGroupDN1 = "CN=PARENT1";
        String parentGroupDN2 = "CN=PARENT2";
        parentGroupsTree.put(dummyGroupDN, new HashSet<>(Arrays.asList(parentGroupDN1, parentGroupDN2)));

        Set<String> expected = new HashSet<>(Arrays.asList(dummyGroupDN, parentGroupDN1, parentGroupDN2));
        assertEquals(expected, cachedParentLdapGroupAuthorityRetriever.retrieveParentGroups(dummyGroupDN));
    }

}