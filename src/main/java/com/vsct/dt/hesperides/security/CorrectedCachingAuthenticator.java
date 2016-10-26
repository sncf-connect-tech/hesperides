/*
 *
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
 *
 */

package com.vsct.dt.hesperides.security;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Optional;
import com.google.common.cache.*;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import static com.codahale.metrics.MetricRegistry.name;

/**
Reuses the CachingAuthenticator code but avoid to cache authentication when it is wrong,
thus allowing the reuse to retry authentication

It has actually been corrected since here -> https://github.com/dropwizard/dropwizard/pull/1084
 */
public class CorrectedCachingAuthenticator<C, P> implements Authenticator<C, P> {
    private final Authenticator<C, P>          underlying;
    private final Cache<C, Optional<P>> cache;
    private final Meter                        cacheMisses;
    private final Timer                        gets;

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param cacheSpec      a {@link CacheBuilderSpec}
     */
    public CorrectedCachingAuthenticator(MetricRegistry metricRegistry,
                                Authenticator<C, P> authenticator,
                                CacheBuilderSpec cacheSpec) {
        this(metricRegistry, authenticator, CacheBuilder.from(cacheSpec));
    }

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param builder        a {@link CacheBuilder}
     */
    public CorrectedCachingAuthenticator(MetricRegistry metricRegistry,
                                Authenticator<C, P> authenticator,
                                CacheBuilder<Object, Object> builder) {
        this.underlying = authenticator;
        this.cacheMisses = metricRegistry.meter(name(authenticator.getClass(), "cache-misses"));
        this.gets = metricRegistry.timer(name(authenticator.getClass(), "gets"));
        this.cache = builder.recordStats().build();
    }

    @Override
    public Optional<P> authenticate(C credentials) throws AuthenticationException {
        final Timer.Context context = gets.time();
        try {
            Optional<P> optionalPrincipal = cache.getIfPresent(credentials);
            if (optionalPrincipal == null) {
                cacheMisses.mark();
                optionalPrincipal = underlying.authenticate(credentials);
                if (optionalPrincipal.isPresent()) {
                    cache.put(credentials, optionalPrincipal);
                }
            }
            return optionalPrincipal;
        } finally {
            context.stop();
        }
    }

    /**
     * Discards any cached principal for the given credentials.
     *
     * @param credentials a set of credentials
     */
    public void invalidate(C credentials) {
        cache.invalidate(credentials);
    }

    /**
     * Discards any cached principal for the given collection of credentials.
     *
     * @param credentials a collection of credentials
     */
    public void invalidateAll(Iterable<C> credentials) {
        cache.invalidateAll(credentials);
    }

    /**
     * Discards all cached principals.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * Returns the number of cached principals.
     *
     * @return the number of cached principals
     */
    public long size() {
        return cache.size();
    }

    /**
     * Returns a set of statistics about the cache contents and usage.
     *
     * @return a set of statistics about the cache contents and usage
     */
    public CacheStats stats() {
        return cache.stats();
    }
}
