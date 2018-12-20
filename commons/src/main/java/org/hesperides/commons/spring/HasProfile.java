package org.hesperides.commons.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Bidouille permettant de détecter un profil dans un contexte statique.
 * Utilisée pour éviter la validation des propriétés lors de la migration de données.
 */
@Component
public class HasProfile {

    private static Environment staticEnvironment;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        staticEnvironment = environment;
    }

    public static boolean dataMigration() {
        if (staticEnvironment == null) {
            // Nécessaire pour que des tests comme domain.templatecontainers.entities.PropertyTest puissent s'exécuter
            return false;
        }
        return isProfileActive(staticEnvironment, SpringProfiles.DATA_MIGRATION);
    }

    public static boolean isProfileActive(Environment environment, String profile) {
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }
}
