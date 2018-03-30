package org.hesperides.infrastructure.redis.eventstores;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.UnableToClaimTokenException;
import org.axonframework.eventsourcing.eventstore.GlobalSequenceTrackingToken;
import org.axonframework.eventsourcing.eventstore.TrackingToken;
import org.hesperides.infrastructure.redis.RedisConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * le store pour les vues Redis.
 * un tocken permet de retrouver l'emplacement du curseur d'alimentation des vues redis.
 * pour mettre à jour une vue, un thread va poller l'event store régulièrement. Le token store est
 * utilisé pour savoir si l'event a déjà été traité par le processor.
 */
@Slf4j
@Component
public class RedisTokenStore implements TokenStore {

    public static final String TOKENS = "a_tokens";
    private final StringRedisTemplate redis;
    private final RedisConfiguration config;

    public RedisTokenStore(StringRedisTemplate template, RedisConfiguration config) {
        this.redis = template;
        this.config = config;
    }

    @PostConstruct
    public void resetStore() {
        if (config.isShouldResetEventTrackingTokenOnStartUp()) {
            this.redis.delete(TOKENS);
        }
    }

    @Override
    public void storeToken(TrackingToken token, String processorName, int segment) throws UnableToClaimTokenException {
        log.debug("store token for processor = {}, segment = {}: {}", processorName, segment, token);
        redis.opsForHash().put(TOKENS,  processorName + "_" + segment, ""+((GlobalSequenceTrackingToken)token).getGlobalIndex());
    }

    @Override
    public TrackingToken fetchToken(String processorName, int segment) throws UnableToClaimTokenException {
        log.debug("fetch token for processor : {}", processorName);

        String hashKey = processorName + "_" + segment;
        if (redis.opsForHash().hasKey(TOKENS, hashKey)) {
            return new GlobalSequenceTrackingToken(Long.parseLong((String)redis.opsForHash().get(TOKENS, hashKey)));
        } else {
            // pas de token: on part du début.
            return new GlobalSequenceTrackingToken(0);
        }
    }

    @Override
    public void releaseClaim(String processorName, int segment) {
        log.debug("release claim");
        // ne fait rien: on ne gère pas la concurrence multi-jvm pour le moment
    }

    @Override
    public int[] fetchSegments(String processorName) {
        log.debug("fetch segments.");
        return new int[0]; // actuellement, ne gère pas les segments car on ne sait pas ce que c'est.
    }

}
