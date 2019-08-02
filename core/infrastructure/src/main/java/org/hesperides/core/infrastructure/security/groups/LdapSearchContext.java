package org.hesperides.core.infrastructure.security.groups;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallExecutorBuilder;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.exception.RetriesExhaustedException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.google.gson.Gson;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.hesperides.core.domain.security.entities.springauthorities.DirectoryGroupDN;
import org.hesperides.core.infrastructure.security.LdapConfiguration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import java.util.*;
import java.util.function.Supplier;


/*
 * Wrapper around javax.naming.* & org.springframework.ldap.* classes & logic,
 * with logging, metrics & retrying.
 *
 * IMPORTANT: instances of this class must be manually closed
 * at the end of their lifespan by calling .closeContext() on them.
 */
@Slf4j
public class LdapSearchContext implements ParentGroupsDNRetriever {

    private final DirContext dirContext;
    private final LdapConfiguration ldapConfiguration;
    private final MeterRegistry meterRegistry;
    private final LdapSearchMetrics ldapSearchMetrics;
    private final RetryConfig retryConfig;
    private final Gson gson;
    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public LdapSearchContext(String username, String password, LdapConfiguration ldapConfiguration,
                             MeterRegistry meterRegistry, LdapSearchMetrics ldapSearchMetrics, RetryConfig retryConfig, Gson gson) {
        this.ldapConfiguration = ldapConfiguration;
        this.meterRegistry = meterRegistry;
        this.ldapSearchMetrics = ldapSearchMetrics;
        this.retryConfig = retryConfig;
        this.gson = gson;
        this.dirContext = withRetry("ldapBuildContext", "building LDAP context for user=" + username,
                                    () -> buildSearchContext(username, password));
    }

    public void closeContext() {
        LdapUtils.closeContext(dirContext); // implique la suppression de l'env créé dans .buildSearchContext
    }

    private DirContext buildSearchContext(String username, String password) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapConfiguration.getUrl());
        env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());
        env.put("com.sun.jndi.ldap.connect.timeout", ldapConfiguration.getConnectTimeout());
        env.put("com.sun.jndi.ldap.read.timeout", ldapConfiguration.getReadTimeout());
        env.put(Context.SECURITY_PRINCIPAL, String.format("%s\\%s", ldapConfiguration.getDomain(), username));
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            DirContext dirContext = new InitialLdapContext(env, null);
            // ici dirContext ne contient que des infos relatives au serveur avec lequel la connexion vient d'être établie
            if (log.isDebugEnabled()) { // on évite ce traitement si ce n'est pas nécessaire
                log.debug("[buildSearchContext] dirContext: {}", gson.toJson(attributesToNative(dirContext.getAttributes("").getAll())));
            }
            return dirContext;
        } catch (AuthenticationException | OperationNotSupportedException cause) {
            throw new BadCredentialsException(messages.getMessage(
                    "LdapAuthenticationProvider.badCredentials", "Bad credentials"), cause);
        } catch (NamingException e) {
            log.error(e.getExplanation() + (e.getCause() != null ? (" : " + e.getCause().getMessage()) : ""));
            throw LdapUtils.convertLdapException(e);
        }
    }

    public HashSet<String> retrieveParentGroupDNs(String dn) {
        HashSet<String> parentGroupDNs = new HashSet<>();
        try {
            String cn = DirectoryGroupDN.extractCnFromDn(dn);
            String base = getBaseFrom(cn, dn);
            String searchFilter = ldapConfiguration.getSearchFilterForCN(cn);
            DirContextOperations dirContextOperations = searchCNWithRetry(cn, base, searchFilter);
            parentGroupDNs = extractDirectParentGroupDNs(dirContextOperations.getAttributes(""));
        } catch (IncorrectResultSizeDataAccessException e) {
            // On accepte que la recherche ne retourne aucun résultat
        } catch (NamingException exception) {
            throw LdapUtils.convertLdapException(exception);
        }
        return parentGroupDNs;
    }

    public DirContextOperations searchUserCNWithRetry(String username) {
        return searchCNWithRetry(username, ldapConfiguration.getUserSearchBase(), ldapConfiguration.getSearchFilterForCN(username));
    }

    private DirContextOperations searchCNWithRetry(String cn, String base, String searchFilter) {
        return withRetry("ldapSearchCN", "requesting LDAP for CN=" + cn,
                         () -> searchCN(dirContext, cn, base, searchFilter));
    }

    private <T> T withRetry(String timerMetricName, String actionDesc, Supplier<T> action) {
        ldapSearchMetrics.incrTotalCallsCounter();
        CallExecutor<T> executor = new CallExecutorBuilder()
                .config(retryConfig)
                .beforeNextTryListener(status -> ldapSearchMetrics.incrTotalCallsCounter())
                .afterFailedTryListener(status -> ldapSearchMetrics.incrFailedCallsCounter())
                .build();
        Timer.Sample sample = Timer.start(meterRegistry);
        String exceptionClass = "none";
        try {
            return executor.execute(action::get).getResult();
        } catch (UnexpectedException exception) {
            Throwable cause = exception.getCause();
            if (!(cause instanceof IncorrectResultSizeDataAccessException)) { // Cette exception est totalement OK et correspond à un cas nominal
                ldapSearchMetrics.incrUnexpectedExceptionCounter();
                exceptionClass = cause.getClass().getSimpleName();
                log.error("Non retry-able exception while " + actionDesc, cause);
            }
            throw (RuntimeException)cause;
        } catch (RetriesExhaustedException exception) {
            ldapSearchMetrics.incrRetriesExhaustedExceptionCounter();
            Throwable cause = exception.getCause();
            exceptionClass = cause.getClass().getSimpleName();
            log.error("Retries exhausted while " + actionDesc, cause);
            throw (RuntimeException)cause;
        } catch (Exception ex) {
            exceptionClass = ex.getClass().getSimpleName();
            throw ex;
        } finally {
            try {
                sample.stop(Timer.builder(timerMetricName)
                        .tags("exception", exceptionClass)
                        .register(meterRegistry));
            } catch (Exception e) {
                // ignoring on purpose
            }
        }
    }

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


    /*****************************************************************/
    /**************** Pretty-printing debug methods ******************/
    /*****************************************************************/

    // ATTENTION: cette méthode CONSOMME l'énumération searchResults, elle a donc un effet de bord
    private static List<Map<String, Object>> searchResultToNative(NamingEnumeration<SearchResult> searchResults) throws NamingException {
        List<Map<String, Object>> output = new ArrayList<>();
        while (searchResults.hasMore()) {
            output.add(attributesToNative(searchResults.next().getAttributes().getAll()));
        }
        return output;
    }

    // ATTENTION: cette méthode CONSOMME l'énumération attributes, elle a donc un effet de bord
    public static Map<String, Object> attributesToNative(NamingEnumeration<? extends Attribute> attributes) throws NamingException {
        Map<String, Object> output = new HashMap<>();
        while (attributes.hasMore()) {
            Attribute attribute = attributes.next();
            output.put(attribute.getID(), attributeValueToNative(attribute));
        }
        return output;
    }

    private static Object attributeValueToNative(Attribute attribute) throws NamingException {
        if (attribute.getID().equals("thumbnailPhoto")) { // integer array value, too long to display
            return "<OMITTED>";
        }
        if (attribute.size() == 1) {
            return attribute.get();
        }
        List<Object> attrs = new ArrayList<>();
        for (int i = 0; i < attribute.size(); i++) {
            attrs.add(attribute.get(i));
        }
        return attrs;
    }

    public DirContextOperations getDirContextOperations() {
        return (DirContextOperations)dirContext;
    }
}
