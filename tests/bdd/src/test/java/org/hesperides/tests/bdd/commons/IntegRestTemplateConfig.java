package org.hesperides.tests.bdd.commons;

import com.google.gson.Gson;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static org.hesperides.commons.spring.SpringProfiles.INTEGRATION_TESTS;
import static org.hesperides.tests.bdd.commons.RestTemplateConfig.configure;

@Configuration
@Profile(INTEGRATION_TESTS)
public class IntegRestTemplateConfig {

    @Bean
    public RestTemplate buildRestTemplate(TestRestTemplate testRestTemplate, Gson gson) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 3128)));
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://hesperides-dev.socrate.vsct.fr:56789"));
        configure(testRestTemplate.getRestTemplate(), gson);
        return restTemplate;
    }
}
