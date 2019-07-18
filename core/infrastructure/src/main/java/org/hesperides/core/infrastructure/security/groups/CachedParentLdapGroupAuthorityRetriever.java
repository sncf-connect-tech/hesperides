package org.hesperides.core.infrastructure.security.groups;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hesperides.core.infrastructure.security.groups.LdapGroupAuthority.containDN;

public class CachedParentLdapGroupAuthorityRetriever {

    static final int MAX_RECURSION = 10; // arbitraire, existe pour éviter tout risque de boucle infinie

    private final Cache cache;
    private ParentGroupsDNRetriever parentGroupsDNRetriever;

    public CachedParentLdapGroupAuthorityRetriever(Cache cache) {
        this.cache = cache;
    }

    public void setParentGroupsDNRetriever(ParentGroupsDNRetriever parentGroupsDNRetriever) {
        this.parentGroupsDNRetriever = parentGroupsDNRetriever;
    }

    public Set<LdapGroupAuthority> retrieveParentGroups(String initialGroupDN) {
        Set<LdapGroupAuthority> allParentGroups = new HashSet<>();
        Set<String> groupDNsAtCurrentLevel = Collections.singleton(initialGroupDN);
        int ancestorLevel;
        for (ancestorLevel = 1; ancestorLevel < MAX_RECURSION; ancestorLevel++) {
            Set<String> groupDNsForNextLevel = new HashSet<>();
            for (String groupDN : groupDNsAtCurrentLevel) {
                allParentGroups.add(new LdapGroupAuthority(groupDN, ancestorLevel));
                Set<String> parentGroupsDN = getParentGroupsDNFromCacheOrFallback(groupDN);
                for (String parentGroupDN : parentGroupsDN) {
                    // On ajoute le groupDN dans la liste de ceux à traiter au prochain niveau
                    // s'il n'est pas déjà dans allParentGroups
                    if (!containDN(allParentGroups, parentGroupDN)) {
                        groupDNsForNextLevel.add(parentGroupDN);
                    }
                }
            }
            if (groupDNsForNextLevel.isEmpty()) {
                break;
            }
            groupDNsAtCurrentLevel = groupDNsForNextLevel;
        }
        if (ancestorLevel >= MAX_RECURSION) {
            throw new ParentGroupRecursionException("Maximum depth of parent group resolution exceeded: " + MAX_RECURSION);
        }
        return allParentGroups;
    }

    private Set<String> getParentGroupsDNFromCacheOrFallback(String groupDN) {
        // Note: on utilise un HashSet et non un Set car il est Serializable, et peut donc être placé en cache
        HashSet<String> parentGroupDN;
        Element element = cache.get(groupDN);
        if (element == null) { // cache miss
            parentGroupDN = parentGroupsDNRetriever.retrieveParentGroupDNs(groupDN);
            cache.put(new Element(groupDN, parentGroupDN));
        } else { // cache hit
            parentGroupDN = (HashSet<String>) element.getObjectValue();
        }
        return parentGroupDN;
    }
}
