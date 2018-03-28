package org.hesperides.infrastructure.redis.eventstores;

import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.UnableToClaimTokenException;
import org.axonframework.eventsourcing.eventstore.TrackingToken;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Connection;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * le store pour les vues Redis.
 * un tocken permet de retrouver l'emplacement du curseur d'alimentation des vues redis.
 * pour mettre à jour une vue, un thread va poller l'event store régulièrement. Le token store est
 * utilisé pour savoir si l'event a déjà été traité par le processor.
 */
@Slf4j
@Component
public class RedisTokenStore implements TokenStore {

    private final StringRedisTemplate redis;

    private final XStream xStream = new XStream();

    public RedisTokenStore(StringRedisTemplate template) {
        this.redis = template;
    }

    @Override
    public void storeToken(TrackingToken token, String processorName, int segment) throws UnableToClaimTokenException {
        log.debug("store token {}, segment = {}", processorName, segment);
        redis.opsForValue().set("a_token_" + processorName + "_" + segment, xStream.toXML(token));
    }

    @Override
    public TrackingToken fetchToken(String processorName, int segment) throws UnableToClaimTokenException {
        log.debug("fetch token");
        return ofNullable(redis.opsForValue().get("a_token_" + processorName + "_" + segment))
                .map(sToken -> {
                    xStream.setClassLoader(Thread.currentThread().getContextClassLoader());
                    return (TrackingToken) xStream.fromXML(sToken);
                }).orElse(null);
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
