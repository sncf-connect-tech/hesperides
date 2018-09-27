package org.hesperides.tests.bdd;

import org.hesperides.HesperidesSpringApplication;
import org.hesperides.tests.bdd.commons.tools.HesperideTestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.NOLDAP;

/**
 * ces tests fonctionnent en mode "RANDOM_PORT", c'est à dire avec un serveur tomcat
 * démarré sur un port random.
 */
@SpringBootTest(classes = HesperidesSpringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {FAKE_MONGO, NOLDAP})
@ContextConfiguration
//@DirtiesContext
public class CucumberSpringBean {
    @Autowired
    protected HesperideTestRestTemplate rest;
}