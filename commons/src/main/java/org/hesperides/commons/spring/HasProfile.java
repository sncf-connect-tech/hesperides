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
        return isProfileActive(SpringProfiles.DATA_MIGRATION);
    }

    public static boolean isProfileActive(String profile) {
        return staticEnvironment != null && Arrays.asList(staticEnvironment.getActiveProfiles()).contains(profile);
    }
}
