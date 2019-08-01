package org.hesperides.core.infrastructure.security.groups;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallExecutorBuilder;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.domain.security.entities.springauthorities.DirectoryGroupDN;
import org.hesperides.core.infrastructure.security.LdapConfiguration;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import java.util.HashSet;

@Slf4j
public class ParentGroupsDNRetrieverFromLdap implements ParentGroupsDNRetriever {

    private final DirContext dirContext;
    private final LdapConfiguration ldapConfiguration;
    private final RetryConfig retryConfig;

    public ParentGroupsDNRetrieverFromLdap(DirContext dirContext, LdapConfiguration ldapConfiguration, RetryConfig retryConfig) {
        this.dirContext = dirContext;
        this.ldapConfiguration = ldapConfiguration;
        this.retryConfig = retryConfig;
    }

    public HashSet<String> retrieveParentGroupDNs(String dn) {
        HashSet<String> parentGroupDNs = new HashSet<>();
        try {
            String cn = DirectoryGroupDN.extractCnFromDn(dn);
            String base = getBaseFrom(cn, dn);
            String searchFilter = ldapConfiguration.getSearchFilterForCN(cn);
            DirContextOperations dirContextOperations = searchCNWithRetry(dirContext, cn, base, searchFilter);
            parentGroupDNs = extractDirectParentGroupDNs(dirContextOperations.getAttributes(""));
        } catch (IncorrectResultSizeDataAccessException e) {
            // On accepte que la recherche ne retourne aucun résultat
        } catch (NamingException exception) {
            throw LdapUtils.convertLdapException(exception);
        }
        return parentGroupDNs;
    }

    @Timed
    public DirContextOperations searchCNWithRetry(DirContext dirContext, String cn, String base, String searchFilter) {
        CallExecutor<DirContextOperations> executor = new CallExecutorBuilder().config(retryConfig).build();
        try {
            return executor.execute(() -> searchCN(dirContext, cn, base, searchFilter)).getResult();
        } catch (UnexpectedException exception) {
            Throwable cause = exception.getCause();
            if (!(cause instanceof IncorrectResultSizeDataAccessException)) { // Cette exception est totalement OK et correspond à un cas nominal
                log.error("Non retry-able exception while requesting LDAP for CN=" + cn, cause);
            }
            throw (RuntimeException)cause;
        } catch (RetriesExhaustedException exception) {
            Throwable cause = exception.getCause();
            log.error("Retries exhausted while requesting LDAP for CN=" + cn, cause);
            throw (RuntimeException)cause;
        }
    }

    @Timed // Il s'agit du seul endroit du code d'où sont véritablement effectués les appels LDAPS
    private static DirContextOperations searchCN(DirContext dirContext, String cn, String base, String searchFilter) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
            // Durant cet appel, SpringSecurityLdapTemplate logue parfois des "Ignoring PartialResultException"
            return SpringSecurityLdapTemplate.searchForSingleEntryInternal(dirContext, searchControls, base, searchFilter, new Object[]{cn});
        } catch (NamingException exception) {
            throw LdapUtils.convertLdapException(exception);
        }
    }

    /**
     * Retourne le DN amputé de son CN
     */
    private static String getBaseFrom(String cn, String dn) {
        return dn.substring(("cn" + cn + ",").length() + 1);
    }

    public static HashSet<String> extractDirectParentGroupDNs(Attributes attributes) {
        try {
            Attribute memberOf = attributes.get("memberOf");
            HashSet<String> groupsDNs = new HashSet<>();
            if (memberOf != null) {
                for (int i = 0; i < memberOf.size(); i++) {
                    groupsDNs.add((String) memberOf.get(i));
                }
            }
            return groupsDNs;
        } catch (NamingException e) {
            throw LdapUtils.convertLdapException(e);
        }
    }
}
