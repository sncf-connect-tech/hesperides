package org.hesperides.core.infrastructure.security.groups;

import java.util.HashSet;

public interface ParentGroupsDNRetriever {

    HashSet<String> retrieveParentGroupDNs(String dn);
}
